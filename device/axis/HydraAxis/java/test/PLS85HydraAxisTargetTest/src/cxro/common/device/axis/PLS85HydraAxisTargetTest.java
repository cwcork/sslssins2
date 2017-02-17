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
 * Program to set up parameters for Micos PLS80 HydraAxis stage.
 * @author cwcork
 */
public class PLS85HydraAxisTargetTest
{
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisTargetTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private HydraComm comm;
    private HydraAxisArray array;
    private HydraAxis axis;

    public PLS85HydraAxisTargetTest(String hostname)
        throws IOException, InterruptedException
    {
        this.comm = new HydraComm(hostname);
        this.array = new HydraAxisArray("/cxro/common/device/axis/HydraAxis/PLS85", this.comm, 1);
        this.axis = array.getAxis(0);

        // Set initial conditions
        // Leave offset and soft limits alone for now
        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setAxisUnits("mm");
        axis.setDefaultSpeed(10.0);
        axis.setDefaultAcceleration(100.0);
        axis.setInitializeSpeed(5.0);

        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PLS85HydraAxisTargetTest test;
        String hostName = null;
        double offset = 0.0;
        double lowerLimit = -200.0;
        double upperLimit = 200.0;
        double midpoint = 0.0;
        Future<Integer> moveOp;
        Future<Integer> stopOp;

        // 0. Turn on full logging
        Logger.getLogger("").setLevel(Level.ALL);
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            handlers[index].setLevel(Level.ALL);
        }

        //1. Get the parameters
        if (args.length != 1)
        {
            printUsage();
            System.exit(1);
        }
        else
        {
            try
            {
                hostName = args[0];
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

            test = new PLS85HydraAxisTargetTest(hostName);
            // Describe setup
            System.out.println("PLS80HydraAxisTargetTest:");
            System.out.println("\tnodeName       = " + test.axis.getName());
            System.out.println("\taxisNumber     = " + test.axis.getAxisNumber());
            System.out.println("\thostName       = " + test.axis.getComm().getHostName());
            System.out.println("\tinitialOffset  = " + test.axis.getOffset());
            System.out.println("\tscale          = " + test.axis.getScale());
            System.out.println("\tlowerLimitHard = " + test.axis.getLowerLimitHard());
            System.out.println("\tupperLimitHard = " + test.axis.getUpperLimitHard());

            // Do simple preparation first
            System.out.println("Enable axis and make sure motor is stopped");
            test.axis.enable();
            stopOp = test.axis.stopMove();
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
            }
            System.out.format("  Stop Status: %d %n", status);
            System.out.format("  isStopped? : %b %n", test.axis.isStopped());
            System.out.format("  isReady?   : %b %n", test.axis.isReady());

            // Get Status
            System.out.println("Get Status");
            System.out.format("  Status: %#x %n", test.axis.getStatus());

            // Get/Set position and offset
            System.out.println("Get position information");
            System.out.format("  offset    : %#f %n", test.axis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.axis.getPosition());
            System.out.format("  speed     : %#f %n", test.axis.getSpeed());

            // INITIALIZE AXIS
            System.out.println("MAKE AXIS READY");
            System.out.println("  Before INITIALIZE");
            System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit SW : %b %n", test.axis.getSwitches()[1]);
            System.out.format("    Lower Limit SW : %b %n", test.axis.getSwitches()[0]);

            System.out.println("  INITIALIZE");
            moveOp = test.axis.initialize();
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
                LOGGER.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                System.exit(1);
            }
            lowerLimit = test.axis.getPosition();
            System.out.println("    Axis Status: " + status);


            System.out.println("  After INITIALIZE");
            System.out.format("    isInitialized?   : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit SW   : %b %n", test.axis.getSwitches()[1]);
            System.out.format("    Lower Limit SW   : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());

             //find upper limit
            System.out.println("Finding upper limit");
            Thread.sleep(500);
            moveOp = test.axis.findUpperLimit(2.5);
            try
            {
                do
                {
                    System.out.format("  position = %#f  \r", test.axis.getPosition());
                    Thread.sleep(100);
                }
                while (!moveOp.isDone());
                status = moveOp.get();
                System.out.println("");
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
            System.out.println("  Axis Status: " + status);

            Thread.sleep(500);
            upperLimit = test.axis.getPosition();

            // Set new soft limits
            System.out.println("Set new soft limits");
            System.out.println("  Before change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());
            test.axis.setUpperLimitHardRaw(test.axis.getPositionRaw() + 1.0);
            test.axis.setUpperLimitSoft(upperLimit + 0.001);
            test.axis.setLowerLimitSoft(lowerLimit -0.001);
            test.axis.setLowerLimitHardRaw(-1.0);
            System.out.println("  After change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());


            // Define new origin at midpoint
            offset = (test.axis.getPositionRaw())/2.0;
            System.out.println("Define new origin at midpoint");
            System.out.println("  Before change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());
            test.axis.setOffset(offset);
            System.out.println("  After change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());

            //move to midpoint
            System.out.println("Moving to origin at midpoint.");
            moveOp = test.axis.moveAbsolute(0.0);
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
            System.out.println("  Axis Status: " + status);

            // status
            System.out.println("At new origin");
            System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[1]);
            System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[0]);

            // Do a series of target move tests
            System.out.println("\nPerform a series of target move tests");

            // Put target at current position
            test.axis.setTarget(0.0);
            System.out.format("  Current Target : %#f %n", test.axis.getTarget());

            // Try moves beyond soft limits
            System.out.println("\n  Attempt moves outside limits");
            status = test.axis.setTarget(100.0);
            System.out.format("    Attempt move to +100.0, result = %d %n", status);
            System.out.format("    Current Target : %#f %n", test.axis.getTarget());
            status = test.axis.setTarget(-100.0);
            System.out.format("    Attempt move to -100.0, result = %d %n", status);
            System.out.format("    Current Target : %#f %n", test.axis.getTarget());

            // Initiate blocking move first
            System.out.println("\n  Attempt target move while blocking move in progress");
            moveOp = test.axis.moveAbsolute(-5.0);
            Thread.sleep(100);
            status = test.axis.setTarget(+10.0);
            System.out.format("    Attempt to setTarget(+10.0) while moveAbsolute(-5.0) in progress; status = %d %n", status);
            System.out.format("    Current Target : %#f %n", test.axis.getTarget());
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
            System.out.println("  Axis Status: " + status);

            // Do a series of oscillating moves
            System.out.println("\n  Do a series of oscillating moves");
            for (int i = 0; i < 10; i++)
            {
                System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
                if (i%2 == 0)
                {
                    test.axis.setTarget(+10.0);
                }
                else
                {
                    test.axis.setTarget(-10.0);
                }
                System.out.format("    %d: Current Target : %#f %n%n", i, test.axis.getTarget());
                Thread.sleep(1000);
            }
            // wait for final move to complete
            System.out.println("  Wait for final move to complete \n");
            try
            {
                do
                {
                    System.out.format("  position = %#f  \r", test.axis.getPosition());
                    Thread.sleep(100);
                }
                while (!test.axis.isReady());
                System.out.println("  Ready");
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
                System.exit(1);
            }

            // leave at midpoint
            System.out.println("\nLeave at midpoint.");
            moveOp = test.axis.moveAbsolute(0.0);
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
            System.out.println("  Axis Status: " + status);

            // Done
            System.out.println("Done");
            System.out.println("  UnInitialize");
            test.axis.setInitialized(false);
            System.out.println("  Disconnect");
            test.axis.getComm().close();
            Thread.sleep(1000);
            System.out.println("  Exit");
            System.exit(0);
        }
        catch (InterruptedException | IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
            "USAGE: java cxro.common.device.axis.PLS85HydraAxisSetup "
            + "<ip number [String]>");
    }
}
