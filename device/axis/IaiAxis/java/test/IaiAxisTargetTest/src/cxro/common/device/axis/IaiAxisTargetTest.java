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
 * Test Program for IAI actuator via the Moxa Modbus/TCP gateway.
 * <p>
 * @author cwcork
 */
public class IaiAxisTargetTest
{
  private static final Logger LOGGER = Logger.getLogger(IaiAxisTargetTest.class.getName());

  static // static configuration
  {
    LOGGER.setLevel(null);
  }

  private static double speed = 10.0;
  private static double acceleration = 2941.995;
  //
  //instance fields
  private IaiAxis axis = null;

  public IaiAxisTargetTest(String axisName,
                           String locator,
                           int axisno)
  throws IOException, InterruptedException
  {
    axis = new IaiAxis(axisName, locator, axisno);

    // Set nominal actuator values
    axis.setOffset(0.0);

    axis.setUpperLimitHardRaw(10030.0);
    axis.setUpperLimitSoft(50.0);
    axis.setLowerLimitSoft(0.0);
    axis.setLowerLimitHardRaw(-30.0);

    axis.setDefaultSpeed(10.0);
    axis.setDefaultAcceleration(2941.995);
    axis.setInitializeSpeed(10.0); // This is actually ignored

    axis.setSpeed(axis.getDefaultSpeed());
    axis.setAcceleration(axis.getDefaultAcceleration());
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    IaiAxisTargetTest test;
    String axisName = "";
    String locator = null;
    int axisno = 0;
    Future<Integer> futOp;

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

    //2. Do the test
    try
    {
      int status;

      test = new IaiAxisTargetTest("/test/cxro/common/device/axis/IaiAxis/" + axisName,
                                   locator,
                                   axisno);
      // Describe setup
      System.out.println("IaiAxisTargetTest:");
      System.out.format("    nodeName         : %s %n", test.axis.getName());
      System.out.format("    scale            : %d %n", test.axis.getAxisNumber());
      System.out.format("    scale            : %#f %n", test.axis.getScale());
      System.out.format("    offset           : %#f %n", test.axis.getOffset());
      System.out.format("    speed            : %#f %n", test.axis.getSpeed());
      System.out.format("    accel            : %#f %n", test.axis.getAcceleration());
      System.out.format("    upperLimitHard   : %#f %n", test.axis.getUpperLimitHard());
      System.out.format("    upperLimitSoft   : %#f %n", test.axis.getUpperLimitSoft());
      System.out.format("    lowerLimitSoft   : %#f %n", test.axis.getLowerLimitSoft());
      System.out.format("    lowerLimitHard   : %#f %n", test.axis.getLowerLimitHard());

      // INITIALIZE AXIS
      System.out.println("\nINITIALIZE AXIS");
      System.out.println("  Before initialize");
      System.out.format("      isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("      ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("      PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("      Position       : %#f %n", test.axis.getPosition());
      System.out.format("      At Upper Limit : %b %n", test.axis.getSwitches()[0]);
      System.out.format("      At Lower Limit : %b %n", test.axis.getSwitches()[1]);

      System.out.println("  Initialize");
      // Initiate
      futOp = test.axis.initialize();
      // Wait for finish
      try
      {
        do
        {
          System.out.format("    Position = %#f  \r", test.axis.getPosition());
          Thread.sleep(100);
        }
        while (!futOp.isDone());
        System.out.println("");
        System.out.format("    Status: %d %n", futOp.get());
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

      System.out.println("  After initialize");
      System.out.format("      isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("      ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("      PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("      Position       : %#f %n", test.axis.getPosition());
      System.out.format("      At Upper Limit : %b %n", test.axis.getSwitches()[0]);
      System.out.format("      At Lower Limit : %b %n", test.axis.getSwitches()[1]);

      // Move to center position
      System.out.println("\nMOVE TO ORIGIN, THEN BEGIN TARGET TESTS");
      System.out.println("  Move to origin ...");
      // Initiate
      futOp = test.axis.moveAbsolute(0.0);
      // Wait for finish
      try
      {
        do
        {
          System.out.format("    Position = %#f  \r", test.axis.getPosition());
          Thread.sleep(100);
        }
        while (!futOp.isDone());
        System.out.println("");
        System.out.format("    Status: %d %n", futOp.get());
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

      System.out.println("  After move");
      System.out.format("      isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("      ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("      PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("      Position       : %#f %n", test.axis.getPosition());
      System.out.format("      At Upper Limit : %b %n", test.axis.getSwitches()[0]);
      System.out.format("      At Lower Limit : %b %n", test.axis.getSwitches()[1]);

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
      futOp = test.axis.moveAbsolute(2.5);
      Thread.sleep(100);
      status = test.axis.setTarget(+5.0);
      System.out.format("    Attempt to setTarget(+5.0) while moveAbsolute(2.5) in progress; status = %d %n", status);
      System.out.format("    Current Target : %#f %n", test.axis.getTarget());
      try
      {
        status = futOp.get();
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

      // Move to oscillation midpoint
      System.out.println("  Move to midpoint ...");
      // Initiate
      futOp = test.axis.moveAbsolute(25.0);
      // Wait for finish
      try
      {
        do
        {
          System.out.format("    Position = %#f  \r", test.axis.getPosition());
          Thread.sleep(100);
        }
        while (!futOp.isDone());
        System.out.println("");
        System.out.format("    Status: %d %n", futOp.get());
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

      // Do a series of oscillating moves
      System.out.println("\n  Do a series of oscillating moves");
      for (int i = 0; i < 10; i++)
      {
        System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
        if (i % 2 == 0)
        {
          test.axis.setTarget(+30.0);
        }
        else
        {
          test.axis.setTarget(+20.0);
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

      // Leave at origin (closed)
      System.out.println("  Leave at origin ...");
      // Initiate
      futOp = test.axis.moveAbsolute(0.0);
      // Wait for finish
      try
      {
        do
        {
          System.out.format("    Position = %#f  \r", test.axis.getPosition());
          Thread.sleep(100);
        }
        while (!futOp.isDone());
        System.out.println("");
        System.out.format("    Status: %d %n", futOp.get());
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

      System.out.println("  After move");
      System.out.format("      isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("      ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("      PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("      Position       : %#f %n", test.axis.getPosition());
      System.out.format("      At Upper Limit : %b %n", test.axis.getSwitches()[0]);
      System.out.format("      At Lower Limit : %b %n", test.axis.getSwitches()[1]);

      // Done
      System.out.println("\nDONE");
      test.axis.destroy();
      Thread.sleep(1000);
      System.out.println("\nEXIT SUCCESS");
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
    "USAGE: java -jar IaiAxisTargetTest.jar "
    + "<axis name [String]> <ipv4 address [String]> <IAI axis number [int]>");
  }
}
