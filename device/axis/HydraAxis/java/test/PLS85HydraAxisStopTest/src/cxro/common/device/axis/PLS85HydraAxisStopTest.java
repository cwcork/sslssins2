// @license
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cwcork
 */
public class PLS85HydraAxisStopTest
{
    // static fields
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisStopTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private HydraAxis axis = null;

    public PLS85HydraAxisStopTest(String axisName,
                              HydraComm port,
                              int axisno,
                              double zeroOffset,
                              double lowerLimit,
                              double upperLimit)
        throws IOException, InterruptedException
    {
        this.axis = new HydraAxis(axisName,
                                  port,
                                  axisno);

        axis.setAxisUnits("mm");
        axis.setScale(1.0);
        axis.setOffset(zeroOffset);

        axis.setLowerLimitHardRaw(-10.0);
        axis.setLowerLimitSoft(lowerLimit);
        axis.setUpperLimitSoft(upperLimit);
        axis.setUpperLimitHardRaw(+60.0);
        axis.setInitializeSpeed(5.0);
        axis.setDefaultSpeed(10.0);
        axis.setDefaultAcceleration(100.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PLS85HydraAxisStopTest test;
        HydraComm comm;
        String axisName = "test";
        String hostName = null;
        int axisno = 1;
        double scale = 1.0;
        double offset = 0.0;
        double lowerLimit = -0.001;
        double upperLimit = 50.001;
        double midpoint = 0.0;
        Future<Integer> moveOp;
        Future<Integer> stopOp;
        Future<Integer> stopOp2 = null;
        
        // 0. Turn on full logging
        Logger.getLogger("").setLevel(Level.ALL);
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            handlers[index].setLevel(Level.ALL);
        }

        //1. Get the parameters
        if (args.length < 3)
        {
            printUsage();
            System.exit(1);
        }
        else
        {
            try
            {
                axisName = args[0];
                hostName = args[1];
                axisno = Integer.parseInt(args[2]);
            }
            catch (Exception ex)
            {
                printUsage();
                System.exit(1);
            }
        }

        //2. Do the tests
        try
        {
            int status = 0;
            double pos;
            double rpos;
            double zoff;

            comm = new HydraComm(hostName);
            test = new PLS85HydraAxisStopTest("/test/cxro/common/device/PLS85HydraAxis/" + axisName,
                                          comm,
                                          axisno,
                                          offset,
                                          lowerLimit,
                                          upperLimit);

            // Describe setup
            System.out.println("HydraAxisTest:");
            System.out.println("\tname = " + test.axis.getName());
            System.out.println("\taxis = " + test.axis.getAxisNumber());
            System.out.println("\tcomm = " + test.axis.getComm().getHostName());
            System.out.println("\toffset = " + test.axis.getOffset());
            System.out.println("\tscale = " + test.axis.getScale());
            System.out.println("\tlowerLimit = " + test.axis.getLowerLimitSoft());
            System.out.println("\tupperLimit = " + test.axis.getUpperLimitSoft());


            // Do simple cleanup first
            System.out.println("Enable Axis");
            //TODO: figure out why the following enable() method hangs the controller.
            test.axis.enable();
            if (!test.axis.isEnabled())
            {
                System.out.println("  PROBLEM : NOT ENABLED");
                System.exit(1);
            }
            
            // Do a series of stops to measure recovery time
            System.out.println("\nPerform a series of stops to measure recovery time");
            long startTime;
            long elapsedTime;
            int iterations = 10;
            
            System.out.format("  Low level with stopMoveSpi, no motion (%d iterations) %n", iterations);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++)
            {
                test.axis.stopMoveSpi();
//                System.out.format("    IsStopped?: %b %n", test.axis.isStopped());
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("    Total time: %d, Loop average: %d in milliseconds %n", elapsedTime, elapsedTime/iterations);
            
            System.out.format("\n  High level with stopMove, no motion (%d iterations) %n", iterations);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++)
            {
                stopOp = test.axis.stopMove();
                try
                {
                    status = (Integer) stopOp.get();
                }
                catch (ExecutionException ex)
                {
                    LOGGER.log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
                catch (CancellationException ex)
                {
                    LOGGER.log(Level.INFO, null, ex);
                    System.exit(1);
                }
//                System.out.format("    Stop Status: %d %n", status);
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("    Total time: %d, Loop average: %d in milliseconds %n", elapsedTime, elapsedTime/iterations);
            
            
            System.out.format("  Low level with stopMoveSpi, with motion (%d iterations) %n", iterations);
            startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++)
            {
                if (i % 2 == 0)
                {
                    test.axis.moveRelativeRawSpi(5.0);
                }
                else
                {
                    test.axis.moveRelativeRawSpi(-5.0);
                }
                Thread.sleep(50);
                test.axis.stopMoveSpi();
                Thread.sleep(50);
                System.out.format("    IsStopped?: %b %n", test.axis.isStopped());
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.format("    Total time: %d, Loop average: %d in milliseconds %n", elapsedTime, elapsedTime/iterations);

            //Relative position test
            System.out.println("moveRelative tests");
            System.out.println("  Moving relative: +2mm");
            moveOp = test.axis.moveRelative(2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("    Move complete.");
            System.out.println("    Current position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            System.out.println("  Moving relative: -2mm");
            moveOp = test.axis.moveRelative(-2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("    Move complete.");
            System.out.println("    Current position: " + test.axis.getPosition() + test.axis.getAxisUnits());

            //Repeat stop tests with real motion
            test.axis.setSpeed(1.0);
            test.axis.setAcceleration(100.0);
            System.out.println("  Next start moving towards fwd limit");
            moveOp = test.axis.moveRelativeRaw(20.0);
            System.out.println("  Aborting in 0.5 Seconds.");
            Thread.sleep(500);
            // Initial stop
            stopOp = test.axis.stopMove();
            // Do a quick succession of stops
            for (int i = 0; i < 5; i++)
            {
                stopOp2 = test.axis.stopMove();
            }
            System.out.println("  Stop Called.");
            // Get status from first stop
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            System.out.println("    First Stop Status: " + status);
            // Get status from last stop
            try
            {
                status = (Integer) stopOp2.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            System.out.println("    Last Stop Status: " + status);
            
            try
            {
                status = (Integer) moveOp.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                System.out.println("Expected Cancellation during STOP");
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            Thread.sleep(1000);
            System.out.println("    Move Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());

            // Done
            System.out.println("Done");
            System.out.println("  Disconnect");
            test.axis.getComm().close();
            Thread.sleep(1000);
            System.out.println("  Exit");
            System.exit(0);
        }
        catch (InterruptedException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
            "java cxro.io.HydraAxisTest_PLS85 "
            + "<axis name [String]> <COM hostname [String]> <axis number [int]>");
    }//printUsage
}
