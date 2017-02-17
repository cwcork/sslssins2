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
public class PLS85HydraAxisTest
{
    // static fields
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private HydraAxis axis = null;

    public PLS85HydraAxisTest(String axisName,
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
        PLS85HydraAxisTest test;
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
            test = new PLS85HydraAxisTest("/test/cxro/common/device/PLS85HydraAxis/" + axisName,
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
            System.out.println("Stop Motor");
            //TODO: figure out why the following enable() method hangs the controller.
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
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            System.out.println("Stop Status: " + status);

            // Get Status
            System.out.println("Get Status");
            System.out.format("  Status: %#x %n", test.axis.getStatus());

            // Get/Set position and offset
            System.out.println("Get position information");
            System.out.format("  offset    : %#f %n", test.axis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.axis.getPosition());
            System.out.format("  speed     : %#f %n", test.axis.getSpeed());

            System.out.println("Offset -> Offset - 1.0");
            test.axis.setOffset(offset - 1.0);
            System.out.format("  offset    : %#f %n", test.axis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.axis.getPosition());

            System.out.println("Restore offset");
            test.axis.setOffset(offset);
            System.out.format("  offset    : %#f %n", test.axis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.axis.getPosition());


            // Get/Set speed
            System.out.println("Get/Set Speed");
            System.out.format("    speed: %#f %n", test.axis.getSpeed());
            System.out.println("  set speed = 1.0");
            test.axis.setSpeed(1.0);
            System.out.format("    speed: %#f %n", test.axis.getSpeed());

            // Get/Set acceleration
            System.out.println("Get/Set Acceleration");
            System.out.format("    accel: %#f %n", test.axis.getAcceleration());
            System.out.println("  set accel = 50.0");
            test.axis.setAcceleration(50.0);
            System.out.format("    accel: %#f %n", test.axis.getAcceleration());

            // restore settings
            System.out.println("Restore settings");
            test.axis.setAcceleration(test.axis.getDefaultAcceleration());
            test.axis.setSpeed(test.axis.getDefaultSpeed());
            System.out.format("  Status: %#x %n", test.axis.getStatus());
            System.out.format("  speed: %#f %n", test.axis.getSpeed());
            System.out.format("  accel: %#f %n", test.axis.getAcceleration());
            System.out.println("");


            // INITIALIZE AXIS
            System.out.println("MAKE AXIS READY");
            System.out.println("  Before INITIALIZE");
            System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[1]);

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
            System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[1]);

            //find upper limit
            System.out.println("Finding upper limit");
            Thread.sleep(500);
            moveOp = test.axis.findUpperLimit(test.axis.getInitializeSpeed());
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
                System.out.println("Cancellation Position (Not Expected): " + test.axis.getPosition() + test.axis.getAxisUnits());
                System.exit(1);
            }
            upperLimit = test.axis.getPosition();
            System.out.println("  Done. Position: " + upperLimit + test.axis.getAxisUnits());
            System.out.println("  Axis Status: " + status);

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
            test.axis.setUpperLimitSoft(upperLimit + 0.001);
            test.axis.setLowerLimitSoft(lowerLimit - 0.001);
            System.out.println("  After change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());


            // Define new origin at midpoint
            midpoint = (upperLimit + lowerLimit) / 2.0;
            System.out.println("Define new origin at midpoint");
            System.out.println("  Before change:");
            System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());
            test.axis.setPosition(midpoint);
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

            //set position to zero
            System.out.println("At new origin");
            System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[1]);

            //Absolute position test
            //Move device to 2mm and then, -2mm
            System.out.println("MoveAbsolute Tests");
            System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits() + " (Should be zero).");
            System.out.println("  Moving to position: 2mm");
            moveOp = test.axis.moveAbsolute(2.0);
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
            System.out.println("  Move complete.");
            System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits() + " (Should be 2mm)");

            System.out.println("  Moving to position: -2mm");
            moveOp = test.axis.moveAbsolute(-2.0);
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
            System.out.println("  Move complete.");
            System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits() + " (Should be -2mm)");

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

            //Change Offset, scale, and units
            System.out.println("");
            System.out.println("Offset/Scale/Units Test");
            System.out.println("  Open up soft limits");

            System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());
            System.out.println("  Move to absoluteRaw = 4.0");
            moveOp = test.axis.moveAbsoluteRaw(4.0);
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

            System.out.println("  Before Change");
            System.out.format("    AxisUnits  : %s %n", test.axis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f %n", test.axis.getOffset());
            System.out.format("    Scale      : %#f %n", test.axis.getScale());
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());

            System.out.println("  New offset = 3.0");
            System.out.println("  New scale  = 0.5");
            System.out.println("  New units  = Pandas");
            test.axis.setOffset(3.0);
            test.axis.setScale(0.5);
            test.axis.setAxisUnits("Pandas");

            System.out.println("  After Change");
            System.out.format("    AxisUnits  : %s %n", test.axis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f (should be 3.0)%n", test.axis.getOffset());
            System.out.format("    Scale      : %#f (should be 0.5)%n", test.axis.getScale());
            System.out.format("    PositionRaw: %#f (should be 4.0)%n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f (should be 2.0)%n", test.axis.getPosition());

            System.out.println("  Move to new zero");
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
            System.out.println("    Axis Status: " + status);
            System.out.println("  Move complete.");
            System.out.format("    PositionRaw: %#f (should be 3.0)%n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f (should be 0.0)%n", test.axis.getPosition());

            System.out.println("  Restore offset and units.");
            test.axis.setOffset(offset);
            test.axis.setScale(scale);
            test.axis.setAxisUnits("mm");
            System.out.format("    AxisUnits  : %s %n", test.axis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f %n", test.axis.getOffset());
            System.out.format("    Scale      : %#f %n", test.axis.getScale());
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());


            //Move to lower limit, set speed and acceleration
            System.out.println("Stop Test");
            System.out.println("  First go to home position");
            moveOp = test.axis.moveAbsolute(0.0);
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
            System.out.println("    Axis Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());
            //Change speed, acceleration
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


            // Leave at midpoint
            System.out.println("Leave stage at midpoint");
            test.axis.setSpeed(test.axis.getDefaultSpeed());
            moveOp = test.axis.moveAbsolute(midpoint);
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
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());

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
