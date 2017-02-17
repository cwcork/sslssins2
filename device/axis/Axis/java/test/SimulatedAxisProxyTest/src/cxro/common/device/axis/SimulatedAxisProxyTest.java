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
public class SimulatedAxisProxyTest
{
    private static final Logger fLogger = Logger.getLogger(SimulatedAxisProxyTest.class.getName());

    static // static configuration
    {
        fLogger.setLevel(null);
    }
    //
    //instance fields
    private AxisProxy fAxis = null;

    public SimulatedAxisProxyTest(String axisName)
      throws IOException, InterruptedException
    {
        fAxis = new AxisProxy(axisName);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        SimulatedAxisProxyTest test;
        String axisName = "";
        double scale = 100.0;
        double offset = 0.0;
        double lowerLimit = 0.0;
        double upperLimit = 200.0;
        double midpoint = 0.0;
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
        if (args.length < 1)
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

            test = new SimulatedAxisProxyTest(axisName);

            // Get initial parameters
            test.fAxis.setScale(scale);
            test.fAxis.setOffset(offset);
            test.fAxis.setLowerLimitSoft(lowerLimit);
            upperLimit = test.fAxis.getUpperLimitHard();
            test.fAxis.setUpperLimitSoft(upperLimit);
            test.fAxis.setAcceleration(acceleration);
            test.fAxis.setSpeed(speed);

            // Describe setup
            System.out.println("AxisTest_Client:");
            System.out.println("\tname = " + test.fAxis.getName());
            System.out.println("\toffset = " + test.fAxis.getOffset());
            System.out.println("\tscale = " + test.fAxis.getScale());
            System.out.println("\tlowerLimit = " + test.fAxis.getLowerLimitHard());
            System.out.println("\tupperLimit = " + test.fAxis.getUpperLimitHard());

            // Do simple cleanup first
            System.out.println("Stop Motor");
            test.fAxis.enable();
            stopOp = test.fAxis.stopMove();
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
            }
            System.out.println("Stop Status: " + status);

            // Get/Set position and offset
            System.out.println("Get position information");
            System.out.format("  offset    : %#f %n", test.fAxis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.fAxis.getPosition());
            System.out.format("  speed     : %#f %n", test.fAxis.getSpeed());

            System.out.println("Offset -> Offset - 1.0");
            test.fAxis.setOffset(offset - 1.0);
            System.out.format("  offset    : %#f %n", test.fAxis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.fAxis.getPosition());

            System.out.println("Restore offset");
            test.fAxis.setOffset(offset);
            System.out.format("  offset    : %#f %n", test.fAxis.getOffset());
            System.out.format("  raw    pos: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", test.fAxis.getPosition());


            // Get/Set speed
            System.out.println("Get/Set Speed");
            System.out.format("    speed: %#f %n", test.fAxis.getSpeed());
            System.out.println("  set speed = 0.5");
            test.fAxis.setSpeed(0.5);
            System.out.format("    speed: %#f %n", test.fAxis.getSpeed());

            // Get/Set acceleration
            System.out.println("Get/Set Acceleration");
            System.out.format("    accel: %#f %n", test.fAxis.getAcceleration());
            System.out.println("  set accel = 5.0");
            test.fAxis.setAcceleration(5.0);
            System.out.format("    accel: %#f %n", test.fAxis.getAcceleration());

            // restore settings
            System.out.println("Restore settings");
            test.fAxis.setAcceleration(acceleration);
            test.fAxis.setSpeed(speed);
            System.out.format("  speed: %#f %n", test.fAxis.getSpeed());
            System.out.format("  accel: %#f %n", test.fAxis.getAcceleration());
            System.out.println("");


            // INITIALIZE AXIS
            System.out.println("MAKE AXIS READY");
            System.out.println("  Before INITIALIZE");
            System.out.format("    isInitialized? : %b %n", test.fAxis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.fAxis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.fAxis.getSwitches()[1]);

            System.out.println("  INITIALIZE");
            moveOp = test.fAxis.initialize();
            try
            {
                status = (Integer) moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);

            System.out.println("  After INITIALIZE");
            System.out.format("    isInitialized? : %b %n", test.fAxis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.fAxis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.fAxis.getSwitches()[1]);

            //find limits
            lowerLimit = test.fAxis.getPosition();
            upperLimit = test.fAxis.getUpperLimitHard();
            System.out.println("  Lower limit position: " + lowerLimit + test.fAxis.getAxisUnits());
            System.out.println("  Upper limit position: " + upperLimit + test.fAxis.getAxisUnits());

            // Set new soft limits
            System.out.println("Set new soft limits");
            System.out.println("  Before change:");
            System.out.format("    ZeroOffset       : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.fAxis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.fAxis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.fAxis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.fAxis.getLowerLimitHard());
            test.fAxis.setUpperLimitSoft(upperLimit - 0.001);
            test.fAxis.setLowerLimitSoft(lowerLimit + 0.001);
            System.out.println("  After change:");
            System.out.format("    ZeroOffset       : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.fAxis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.fAxis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.fAxis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.fAxis.getLowerLimitHard());


            // Define new origin at midpoint
            midpoint = (upperLimit + lowerLimit) / 2.0;
            System.out.println("Define new origin at midpoint");
            System.out.println("  Before change:");
            System.out.format("    ZeroOffset       : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.fAxis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.fAxis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.fAxis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.fAxis.getLowerLimitHard());
            test.fAxis.setPosition(-midpoint);
            System.out.println("  After change:");
            System.out.format("    ZeroOffset       : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position         : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit Hard : %#f %n", test.fAxis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.fAxis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.fAxis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.fAxis.getLowerLimitHard());

            //move to midpoint
            System.out.println("Moving to origin at midpoint.");
            moveOp = test.fAxis.moveAbsolute(0.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);

            //set position to zero
            System.out.println("At new origin");
            System.out.format("    isInitialized? : %b %n", test.fAxis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", test.fAxis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position       : %#f %n", test.fAxis.getPosition());
            System.out.format("    Upper Limit    : %b %n", test.fAxis.getSwitches()[0]);
            System.out.format("    Lower Limit    : %b %n", test.fAxis.getSwitches()[1]);

            //Do a series of stopMove tests
            System.out.println("");
            System.out.println("Do a series of stopMove tests");
            test.fAxis.setSpeed(1.0);
            test.fAxis.setAcceleration(100.0);

            for (int i = 0; i < 2; i++)
            {
                System.out.println("  Move to +10.0");
                moveOp = test.fAxis.moveAbsolute(10.0);
                System.out.println("  Stopping in 2.0 Seconds.");
                Thread.sleep(2000);
                stopOp = test.fAxis.stopMove();
                System.out.println("  Stop Called.");
                //Get status from stopOp
                try
                {
                    status = (Integer) stopOp.get();
                }
                catch (ExecutionException ex)
                {
                    fLogger.log(Level.SEVERE, null, ex);
                }
                catch (CancellationException ex)
                {
                    fLogger.log(Level.INFO, null, ex);
                    System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                }
                System.out.println("    Stop Status: " + status);
                //Also look at status from moveOp
                try
                {
                    status = (Integer) moveOp.get();
                }
                catch (ExecutionException ex)
                {
                    fLogger.log(Level.SEVERE, null, ex);
                }
                catch (CancellationException ex)
                {
                    fLogger.log(Level.INFO, null, ex);
                    System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                }
                Thread.sleep(1000);
                System.out.println("    Move Status: " + status);
                System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
                System.out.format("    Position   : %#f %n", test.fAxis.getPosition());

                System.out.println("  Move to -10.0");
                moveOp = test.fAxis.moveAbsolute(-10.0);
                System.out.println("  Stopping in 2.0 Seconds.");
                Thread.sleep(2000);
                stopOp = test.fAxis.stopMove();
                System.out.println("  Stop Called.");
                //Get status from stopOp
                try
                {
                    status = (Integer) stopOp.get();
                }
                catch (ExecutionException ex)
                {
                    fLogger.log(Level.SEVERE, null, ex);
                }
                catch (CancellationException ex)
                {
                    fLogger.log(Level.INFO, null, ex);
                    System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                }
                System.out.println("    Stop Status: " + status);
                //Also look at status from moveOp
                try
                {
                    status = (Integer) moveOp.get();
                }
                catch (ExecutionException ex)
                {
                    fLogger.log(Level.SEVERE, null, ex);
                }
                catch (CancellationException ex)
                {
                    fLogger.log(Level.INFO, null, ex);
                    System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                }
                Thread.sleep(1000);
                System.out.println("    Move Status: " + status);
                System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
                System.out.format("    Position   : %#f %n", test.fAxis.getPosition());
            }

            //Absolute position test
            //Move device to +2mm and then, -2mm
            System.out.println("MoveAbsolute Tests");
            System.out.println("  Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits() + " (Should be zero).");
            System.out.println("  Moving to position: 2mm");
            moveOp = test.fAxis.moveAbsolute(2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);
            System.out.println("  Move complete.");
            System.out.println("  Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits() + " (Should be 2mm)");
            System.out.println("  Moving to position: -2mm");
            moveOp = test.fAxis.moveAbsolute(-2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);
            System.out.println("  Move complete.");
            System.out.println("  Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits() + " (Should be -2mm)");

            //Relative position test
            //Move by +2mm, then -2mm
            System.out.println("moveRelative tests");
            System.out.println("  Moving relative: +2mm");
            moveOp = test.fAxis.moveRelative(2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("    Move complete.");
            System.out.println("    Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
            System.out.println("  Moving relative: -2mm");
            moveOp = test.fAxis.moveRelative(-2.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("    Move complete.");
            System.out.println("    Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());


            //Change Offset, scale, and units
            System.out.println("");
            System.out.println("Offset/Scale/Units Test");
            System.out.println("  Open up soft limits");

            System.out.format("    Upper Limit Hard : %#f %n", test.fAxis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", test.fAxis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", test.fAxis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", test.fAxis.getLowerLimitHard());
            System.out.println("  Move to absoluteRaw = 4.0");
            moveOp = test.fAxis.moveAbsoluteRaw(4.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("    Move complete.");
            System.out.println("    Current position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());

            System.out.println("  Before Change");
            System.out.format("    AxisUnits  : %s %n", test.fAxis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f %n", test.fAxis.getOffset());
            System.out.format("    Scale      : %#f %n", test.fAxis.getScale());
            System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.fAxis.getPosition());

            System.out.println("  New offset = 3.0");
            System.out.println("  New scale  = 0.5");
            System.out.println("  New units  = Pandas");
            test.fAxis.setOffset(3.0);
            test.fAxis.setScale(0.5);
            test.fAxis.setAxisUnits("Pandas");

            System.out.println("  After Change");
            System.out.format("    AxisUnits  : %s %n", test.fAxis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f (should be 3.0)%n", test.fAxis.getOffset());
            System.out.format("    Scale      : %#f (should be 0.5)%n", test.fAxis.getScale());
            System.out.format("    PositionRaw: %#f (should be 4.0)%n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f (should be 2.0)%n", test.fAxis.getPosition());

            System.out.println("  Move to new zero");
            moveOp = test.fAxis.moveAbsolute(0.0);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.println("  Move complete.");
            System.out.format("    PositionRaw: %#f (should be 3.0)%n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f (should be 0.0)%n", test.fAxis.getPosition());

            System.out.println("  Restore offset and units.");
            test.fAxis.setOffset(offset);
            test.fAxis.setScale(scale);
            test.fAxis.setAxisUnits("mm");
            System.out.format("    AxisUnits  : %s %n", test.fAxis.getAxisUnits());
            System.out.format("    ZeroOffset : %#f %n", test.fAxis.getOffset());
            System.out.format("    Scale      : %#f %n", test.fAxis.getScale());
            System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.fAxis.getPosition());


            //Move to lower limit, set speed and acceleration
            System.out.println("Stop Test.");
            System.out.println("  Move to lower limit");
            moveOp = test.fAxis.moveAbsoluteRaw(0.0);
            try
            {
                status = (Integer) moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.fAxis.getPosition());
            //Change speed, acceleration
            test.fAxis.setSpeed(1.0);
            test.fAxis.setAcceleration(100.0);
            System.out.println("  Running to other end.");
            moveOp = test.fAxis.moveRelative(20.0);
            System.out.println("  Stopping in 2.0 Seconds.");
            Thread.sleep(2000);
            stopOp = test.fAxis.stopMove();
            System.out.println("  Stop Called.");
            //Get status from stopOp
            try
            {
                status = (Integer) stopOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
            }
            System.out.println("    Stop Status: " + status);
            //Also look at status from moveOp
            try
            {
                status = (Integer) moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
            }
            Thread.sleep(1000);
            System.out.println("    Move Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.fAxis.getPosition());


            //Make sure we are still enabled
            System.out.println("Make certain we are still enabled");
            System.out.format("  Before, isEnabled: %b %n", test.fAxis.isEnabled());
            test.fAxis.enable();
            System.out.format("  After,  isEnabled: %b %n", test.fAxis.isEnabled());


            // Leave at midpoint
            System.out.println("Leave stage at midpoint");
            moveOp = test.fAxis.moveAbsolute(midpoint);
            try
            {
                status = moveOp.get();
            }
            catch (ExecutionException ex)
            {
                fLogger.log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            catch (CancellationException ex)
            {
                fLogger.log(Level.INFO, null, ex);
                System.out.println("Cancellation Position: " + test.fAxis.getPosition() + test.fAxis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("    Axis Status: " + status);
            System.out.format("    PositionRaw: %#f %n", test.fAxis.getPositionRaw());
            System.out.format("    Position   : %#f %n", test.fAxis.getPosition());


            // Done
            System.out.println("Done");
            test.fAxis.destroy();
            Thread.sleep(1000);
            System.out.println("  Exit");
            System.exit(0);
        }
        catch (InterruptedException | IOException ex)
        {
            fLogger.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
          "java cxro.common.device.axis.Client "
          + "<axis name [String]>");
    }
}
