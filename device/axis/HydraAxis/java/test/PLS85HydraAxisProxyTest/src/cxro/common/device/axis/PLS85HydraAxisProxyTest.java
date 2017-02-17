// @license
package cxro.common.device.axis;

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
public class PLS85HydraAxisProxyTest
{
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisProxyTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private AxisProxy axis = null;

    public PLS85HydraAxisProxyTest(String axisName)
      throws IOException, InterruptedException
    {
        axis = new AxisProxy(axisName);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PLS85HydraAxisProxyTest test;
        String axisName = "";
        double scale = 1.0;
        double offset = 0.0;
        double lowerLimit = 0.0;
        double upperLimit = 50.0;
        double midpoint = 25.0;
        double speed = 2.0;
        double acceleration = 10.0;
        double initializeSpeed = 1.0;

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
                axisName = args[0];
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

            test = new PLS85HydraAxisProxyTest(axisName);

            // Get initial parameters
            test.axis.setScale(scale);
            test.axis.setOffset(offset);
            lowerLimit = test.axis.getLowerLimitHard() + 0.009;
            test.axis.setLowerLimitSoft(lowerLimit);
            upperLimit = test.axis.getUpperLimitHard() - 0.009;
            test.axis.setUpperLimitSoft(upperLimit);
            test.axis.setAcceleration(acceleration);
            test.axis.setSpeed(speed);

            // Describe setup
            System.out.println("PLS85HydraAxisProxyTest:");
            System.out.println("\tname           = " + test.axis.getName());
            System.out.println("\toffset         = " + test.axis.getOffset());
            System.out.println("\tscale          = " + test.axis.getScale());
            System.out.println("\tupperLimitHard = " + test.axis.getUpperLimitHard());
            System.out.println("\tupperLimitSoft = " + test.axis.getUpperLimitSoft());
            System.out.println("\tlowerLimitSoft = " + test.axis.getLowerLimitSoft());
            System.out.println("\tlowerLimitHard = " + test.axis.getLowerLimitHard());

            // Do simple cleanup first
            System.out.println("Stop Motor");
            test.axis.enable();
            stopOp = test.axis.stopMove();
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                System.out.println("  CancellationException : EXPECTED");
                System.out.println("  Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            catch (CancellationException ex)
            {
                System.out.println("  CancellationException : EXPECTED");
                System.out.println("  Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            System.out.println("Stop Status: " + status);

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
            System.out.println("  set speed = 0.5");
            test.axis.setSpeed(0.5);
            System.out.format("    speed: %#f %n", test.axis.getSpeed());

            // Get/Set acceleration
            System.out.println("Get/Set Acceleration");
            System.out.format("    accel: %#f %n", test.axis.getAcceleration());
            System.out.println("  set accel = 5.0");
            test.axis.setAcceleration(5.0);
            System.out.format("    accel: %#f %n", test.axis.getAcceleration());

            // restore settings
            System.out.println("Restore settings");
            test.axis.setAcceleration(acceleration);
            test.axis.setSpeed(speed);
            System.out.format("  speed: %#f %n", test.axis.getSpeed());
            System.out.format("  accel: %#f %n", test.axis.getAcceleration());
            System.out.println("");


            // INITIALIZE AXIS
            System.out.println("MAKE AXIS READY");
            System.out.println("  Before INITIALIZE");
            System.out.format("    isInitialized?     : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset         : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw        : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position           : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Switch : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Lower Limit Switch : %b %n", test.axis.getSwitches()[1]);

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
            System.out.println("    Axis Status: " + status);

            System.out.println("  After INITIALIZE");
            System.out.format("    isInitialized?     : %b %n", test.axis.isInitialized());
            System.out.format("    ZeroOffset         : %#f %n", test.axis.getOffset());
            System.out.format("    PositionRaw        : %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position           : %#f %n", test.axis.getPosition());
            System.out.format("    Upper Limit Switch : %b %n", test.axis.getSwitches()[0]);
            System.out.format("    Lower Limit Switch : %b %n", test.axis.getSwitches()[1]);

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
            lowerLimit = test.axis.getPosition();
            upperLimit = test.axis.getUpperLimitHard() - 0.01;
            test.axis.setUpperLimitSoft(upperLimit);
            test.axis.setLowerLimitSoft(lowerLimit);
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
            test.axis.setPosition(-midpoint);
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

            //Do a series of stopMove tests
            System.out.println("");
            System.out.println("Do a series of stopMove tests");
            test.axis.setSpeed(1.0);
            test.axis.setAcceleration(100.0);

            for (int i = 0; i < 2; i++)
            {
                System.out.println("  Move to +10.0");
                moveOp = test.axis.moveAbsolute(10.0);
                System.out.println("  Stopping in 2.0 Seconds.");
                Thread.sleep(2000);
                stopOp = test.axis.stopMove();
                System.out.println("  Stop Called.");
                //Get status from stopOp
                try
                {
                    status = (Integer) stopOp.get();
                }
                catch (ExecutionException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                catch (CancellationException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                System.out.println("    Stop Status: " + status);
                //Also look at status from moveOp
                try
                {
                    status = (Integer) moveOp.get();
                }
                catch (ExecutionException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                catch (CancellationException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                Thread.sleep(1000);
                System.out.println("    Move Status: " + status);
                System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
                System.out.format("    Position   : %#f %n", test.axis.getPosition());

                System.out.println("  Move to -10.0");
                moveOp = test.axis.moveAbsolute(-10.0);
                System.out.println("  Stopping in 2.0 Seconds.");
                Thread.sleep(2000);
                stopOp = test.axis.stopMove();
                System.out.println("  Stop Called.");
                //Get status from stopOp
                try
                {
                    status = (Integer) stopOp.get();
                }
                catch (ExecutionException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                catch (CancellationException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                System.out.println("    Stop Status: " + status);
                //Also look at status from moveOp
                try
                {
                    status = (Integer) moveOp.get();
                }
                catch (ExecutionException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                catch (CancellationException ex)
                {
                    System.out.println("    CancellationException : EXPECTED");
                    System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
                }
                Thread.sleep(1000);
                System.out.println("    Move Status: " + status);
                System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
                System.out.format("    Position   : %#f %n", test.axis.getPosition());
            }

            //Absolute position test
            //Move device to +2mm and then, -2mm
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
            //Move by +2mm, then -2mm
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
            System.out.println("Stop Test.");
            System.out.println("  Move to lower limit");
            moveOp = test.axis.moveAbsoluteRaw(0.0);
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
            System.out.println("  Running towards other end.");
            moveOp = test.axis.moveRelative(20.0);
            System.out.println("  Stopping in 2.0 Seconds.");
            Thread.sleep(2000);
            stopOp = test.axis.stopMove();
            System.out.println("  Stop Called.");
            //Get status from stopOp
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                System.out.println("    CancellationException : EXPECTED");
                System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            System.out.println("    Stop Status: " + status);
            //Also look at status from moveOp
            try
            {
                status = (Integer) moveOp.get();
            }
            catch (ExecutionException ex)
            {
                System.out.println("    CancellationException : EXPECTED");
                System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            catch (CancellationException ex)
            {
                System.out.println("    CancellationException : EXPECTED");
                System.out.println("    Cancellation Position: " + test.axis.getPosition() + test.axis.getAxisUnits());
            }
            Thread.sleep(1000);
            System.out.println("    Move Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.axis.getPosition());


            //Make sure we are still enabled
            System.out.println("Make certain we are still enabled");
            System.out.format("  Before, isEnabled: %b %n", test.axis.isEnabled());
            test.axis.enable();
            System.out.format("  After,  isEnabled: %b %n", test.axis.isEnabled());


            // Leave at midpoint
            System.out.println("Leave stage at midpoint");
            test.axis.setSpeed(speed);
            test.axis.setAcceleration(acceleration);
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
            test.axis.destroy();
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
          "Usage: java cxro.common.device.axis.PLS85HydraAxisProxyTest "
          + "<axis name [String]>");
    }
}
