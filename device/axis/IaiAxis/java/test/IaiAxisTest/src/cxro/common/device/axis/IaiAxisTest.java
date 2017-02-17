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
 * Test Program for IAI actuator via the Moxa Modbus/TCP gateway
 * <p>
 * @author cwcork
 */
public class IaiAxisTest
{
  private static final Logger logger = Logger.getLogger(IaiAxisTest.class.getName());

  static // static configuration
  {
    logger.setLevel(null);
  }

  private static double speed = 10.0;
  private static double acceleration = 2941.995;
  //
  //instance fields
  private IaiAxis axis = null;

  public IaiAxisTest(String axisName,
                     String locator,
                     int axisno)
  throws IOException, InterruptedException
  {
    axis = new IaiAxis(axisName, locator, axisno);
    
    // Set nominal actuator values
    axis.setDefaultAcceleration(2941.995);
    axis.setDefaultSpeed(10.0);
    axis.setInitializeSpeed(10.0); // This is actually ignored
    axis.setUpperLimitHardRaw(10030.0);
    axis.setLowerLimitHardRaw(-30.0);
    axis.setOffset(0.0);
    axis.setUpperLimitSoft(50.0);
    axis.setLowerLimitSoft(0.0);
    axis.setSpeed(axis.getDefaultSpeed());
    axis.setAcceleration(axis.getDefaultAcceleration());
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    IaiAxisTest test;
    String axisName = "";
    String locator = null;
    int axisno = 0;
    Future<Integer> moveOp;
    Future<Integer> stopOp;

    // 0. Set log level
    Logger.getLogger("").setLevel(Level.INFO); // INFO only
//    Logger.getLogger("").setLevel(Level.ALL); // ALL levels
    Handler[] handlers = Logger.getLogger("").getHandlers();
    for (Handler handler : handlers)
    {
      handler.setLevel(Level.ALL);
    }

    //1. Get the parameters
    if (args.length != 3)
    {
      printUsage();
      System.exit(1);
    }
    else
    {
      try
      {
        axisName = args[0];
        locator = args[1];
        axisno = Integer.parseInt(args[2]);
      }
      catch (NumberFormatException ex)
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
      double scale;
      double offset;
      double lowerLimit;
      double upperLimit;

      test = new IaiAxisTest("/test/cxro/common/device/axis/IaiAxis/" + axisName,
                             locator,
                             axisno);

      // Get initial scale and offset
      scale = test.axis.getScale();
      offset = test.axis.getOffset();

      // Describe setup
      System.out.println("IaiAxisTest:");
      System.out.println("\tnodeName       = " + test.axis.getName());
      System.out.println("\taxisNumber     = " + test.axis.getAxisNumber());
      System.out.println("\thostName       = " + test.axis.getLocator());
      System.out.println("\tinitialOffset  = " + test.axis.getOffset());
      System.out.println("\tscale          = " + test.axis.getScale());
      System.out.println("\tauxOffset      = " + test.axis.getAuxEncoderOffset());
      System.out.println("\tauxscale       = " + test.axis.getAuxEncoderScale());
      System.out.println("\tlowerLimitHard = " + test.axis.getLowerLimitHard());
      System.out.println("\tupperLimitHard = " + test.axis.getUpperLimitHard());

      // Do simple preparation first
      System.out.println("Enable axis and make sure motor is stopped");
      System.out.println("\tinitial ALMC   = " + test.axis.getAlarmCode());
      test.axis.reset();
      test.axis.enable();
      stopOp = test.axis.stopMove();
      try
      {
        status = stopOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
      }
      System.out.println("\tstop status    = " + status);
      System.out.println("\tfinal ALMC     = " + test.axis.getAlarmCode());

      // Get Status
      System.out.println("Get Status");
      System.out.format("  Status: %#x %n", test.axis.getStatus());

      // Get/Set position and offset
      System.out.println("Get position information");
      System.out.format("  offset    : %#f %n", test.axis.getOffset());
      System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
      System.out.format("  cooked pos: %#f %n", test.axis.getPosition());
      System.out.format("  speed     : %#f %n", test.axis.getSpeed());

      System.out.println("Offset -> Offset - 1.0mm");
      test.axis.setOffset(offset - (1.0 * scale));
      System.out.format("  offset    : %#f %n", test.axis.getOffset());
      System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
      System.out.format("  cooked pos: %#f %n", test.axis.getPosition());

      System.out.println("Restore offset");
      test.axis.setOffset(offset);
      System.out.format("  offset    : %#f %n", test.axis.getOffset());
      System.out.format("  raw    pos: %#f %n", test.axis.getPositionRaw());
      System.out.format("  cooked pos: %#f %n", test.axis.getPosition());

      // Get/Set speed
      speed = test.axis.getSpeed();
      System.out.println("Get/Set Speed");
      System.out.format("    speed: %#f %n", speed);
      System.out.println("  set speed = 0.5");
      test.axis.setSpeed(0.5);
      System.out.format("    speed: %#f %n", test.axis.getSpeed());

      // Get/Set acceleration
      acceleration = test.axis.getAcceleration();
      System.out.println("Get/Set Acceleration");
      System.out.format("    accel: %#f %n", acceleration);
      System.out.println("  set accel = 980.665");
      test.axis.setAcceleration(980.665);
      System.out.format("    accel: %#f %n", test.axis.getAcceleration());

      // restore settings
      System.out.println("Restore settings");
      test.axis.setAcceleration(acceleration);
      test.axis.setSpeed(speed);
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
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("    Axis Status: " + status);

      System.out.println("  After INITIALIZE");
      System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position       : %#f %n", test.axis.getPosition());
      System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[0]);
      System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[1]);

      // Set new soft limits
      upperLimit = test.axis.getUpperLimitSoft();
      lowerLimit = test.axis.getLowerLimitSoft();
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
      double midpoint = (upperLimit + lowerLimit) / 2.0;
      System.out.println("Define new origin at midpoint");
      System.out.println("  Before change:");
      System.out.format("    ZeroOffset       : %#f %n", test.axis.getOffset());
      System.out.format("    PositionRaw      : %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position         : %#f %n", test.axis.getPosition());
      System.out.format("    Upper Limit Hard : %#f %n", test.axis.getUpperLimitHard());
      System.out.format("    Upper Limit Soft : %#f %n", test.axis.getUpperLimitSoft());
      System.out.format("    Lower Limit Soft : %#f %n", test.axis.getLowerLimitSoft());
      System.out.format("    Lower Limit Hard : %#f %n", test.axis.getLowerLimitHard());
      test.axis.setPosition(test.axis.getPosition() - midpoint);
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
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("  Axis Status: " + status);

      //verify we are at zero
      System.out.println("At new origin");
      System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position       : %#f %n", test.axis.getPosition());
      System.out.format("    Upper Limit    : %b %n", test.axis.getSwitches()[0]);
      System.out.format("    Lower Limit    : %b %n", test.axis.getSwitches()[1]);

      //Absolute position test
      //Move device to +2mm and then, -2mm
      System.out.println("MoveAbsolute Tests");
      System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits()
                         + " (Should be zero).");
      System.out.println("  Moving to position: 2mm");
      moveOp = test.axis.moveAbsolute(2.0);
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("  Axis Status: " + status);
      System.out.println("  Move complete.");
      System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits()
                         + " (Should be 2mm)");
      System.out.println("  Moving to position: -2mm");
      moveOp = test.axis.moveAbsolute(-2.0);
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("  Axis Status: " + status);
      System.out.println("  Move complete.");
      System.out.println("  Current position: " + test.axis.getPosition() + test.axis.getAxisUnits()
                         + " (Should be -2mm)");

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
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("    Axis Status: " + status);
      System.out.println("    Move complete.");
      System.out.println("    Current position: " + test.axis.getPosition() + test.axis.
      getAxisUnits());
      System.out.println("  Moving relative: -2mm");
      moveOp = test.axis.moveRelative(-2.0);
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
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
      
      System.out.println("  Move to absoluteRaw = 1000.0");
      moveOp = test.axis.moveAbsoluteRaw(1000.0);
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " 
                           + test.axis.getPosition() + test.axis.getAxisUnits());
        System.exit(1);
      }
      System.out.println("    Axis Status: " + status);
      System.out.println("    Move complete.");
      System.out.println("    Current position: " 
                         + test.axis.getPosition() + test.axis.getAxisUnits());

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
      System.out.format("    PositionRaw: %#f (should be 1000.0)%n", test.axis.getPositionRaw());
      System.out.format("    Position   : %#f (should be 1994.0)%n", test.axis.getPosition());

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
      System.out.println("  Move to near lower limit");
      moveOp = test.axis.moveAbsolute(test.axis.getLowerLimitSoft() + 5.0);
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("    Axis Status: " + status);
      System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position   : %#f %n", test.axis.getPosition());
      //Change speed, acceleration
      test.axis.setSpeed(1.0);
      test.axis.setAcceleration(500.0);
      System.out.println("  Running to other end.");
      moveOp = test.axis.moveRelative(20.0);
      System.out.println("  Stopping in 2.0 Seconds.");
      Thread.sleep(2000);
      stopOp = test.axis.stopMove();
      System.out.println("  Stop Called.");
      //Get status from stopOp
      try
      {
        status = stopOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
      }
      System.out.println("    Stop Status: " + status);
      //Also look at status from moveOp
      try
      {
        status = moveOp.get();
      }
      catch (ExecutionException ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
      }
      System.out.println("    Move Status: " + status);
      System.out.format("    PositionRaw: %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position   : %#f %n", test.axis.getPosition());

      //Make sure we are still enabled
      System.out.println("Make certain we are still enabled");
      System.out.format("  Before, isEnabled: %b %n", test.axis.isEnabled());
      test.axis.enable();
      System.out.format("  After,  isEnabled: %b %n", test.axis.isEnabled());

      // Leave at midpoint
      Thread.sleep(1000);
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
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.out.println("Cancellation Position: " + test.axis.getPosition() + test.axis.
        getAxisUnits());
        System.exit(1);
      }
      System.out.println("    Axis Status: " + status);
      System.out.format("    Offset      : %#f %n", test.axis.getOffset());
      System.out.format("    PositionRaw : %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position    : %#f %n", test.axis.getPosition());
      System.out.format("    Speed       : %#f %n", test.axis.getSpeed());
      System.out.format("    Accel       : %#f %n", test.axis.getAcceleration());

      // Done
      System.out.println("Done");
      System.out.println("  Disconnect");
      test.axis.destroy();
      Thread.sleep(1000);
      System.out.println("  Exit");
      System.exit(0);
    }
    catch (InterruptedException | IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  private static void printUsage()
  {
    System.out.println(
    "java -jar IaiAxisTest.jar "
    + "<axis name [String]> <ipv4 address [String]> <IAI axis number [int]>");
  }
}
