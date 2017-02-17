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
 * Program to set up parameters for IAI actuator via the Modbus/TCP gateway.
 * <p>
 * @author cwcork
 */
public class IaiAxisSetup
{
  private static final Logger logger = Logger.getLogger(IaiAxisSetup.class.getName());

  static // static configuration
  {
    logger.setLevel(null);
  }
  //
  //instance fields
  private IaiAxis axis = null;

  public IaiAxisSetup(String axisName,
                      String locator,
                      int axisno)
  throws IOException, InterruptedException
  {
    axis = new IaiAxis(axisName, locator, axisno);
    
    axis.setDefaultAcceleration(2941.995); // 0.3 G = 30 counts/s^2
    axis.setDefaultSpeed(10.0); // 1000 counts/s
    axis.setInitializeSpeed(10.0); // This is actually ignored
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    IaiAxisSetup test;
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
      double zoff;

      test = new IaiAxisSetup("/test/cxro/common/device/axis/IaiAxis/" + axisName,
                              locator,
                              axisno);
      // Describe setup
      System.out.println("IaiAxisSetup:");
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
        do
        {
          System.out.format("    Position = %#f  \r", test.axis.getPosition());
          Thread.sleep(100);
        }
        while (!moveOp.isDone());
        System.out.format("\n    Status: %d %n", moveOp.get());
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

      System.out.println("  After INITIALIZE");
      System.out.format("    isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("    ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("    PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("    Position       : %#f %n", test.axis.getPosition());
      System.out.format("    Upper Limit SW : %b %n", test.axis.getSwitches()[1]);
      System.out.format("    Lower Limit SW : %b %n", test.axis.getSwitches()[0]);

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
    "USAGE: java -jar IaiAxisSetup.jar "
    + "<axis name [String]> <ipv4 address [String]> <IAI axis number [int]>");
  }
}
