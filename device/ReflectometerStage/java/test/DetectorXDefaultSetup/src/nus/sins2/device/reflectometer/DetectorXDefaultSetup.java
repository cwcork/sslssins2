// @license
package nus.sins2.device.reflectometer;

import cxro.common.device.axis.Axis;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test Program for NUS.SINS2 Reflectometer DetectorX stage default setup
 * <p>
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public class DetectorXDefaultSetup
{
  private static final Logger logger = Logger.getLogger(DetectorXDefaultSetup.class.getName());
  private static final String DEFAULT_NODENAME = "/nus/sins2/device/reflectometer/stage";
  private static final double VENDOR_OFFSET = 75.00;
  
  static // static configuration
  {
    logger.setLevel(null);
  }
  //
  //instance fields
  private ReflectometerStage stage = null;
  private Axis axis = null;

  public DetectorXDefaultSetup(String nodeName)
  throws IOException, InterruptedException
  {
    stage = new ReflectometerStage(nodeName);
    axis = stage.getDetectorX();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    DetectorXDefaultSetup test;
    String nodeName = "";
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
    switch (args.length)
    {
      case 1:
        nodeName = args[0];
        break;

      case 0:
        nodeName = DEFAULT_NODENAME;
        break;

      default:
        printUsage();
        System.exit(1);
    }

    //2. Do the setup
    try
    {
      int status;

      test = new DetectorXDefaultSetup(nodeName);

      // Describe setup
      System.out.println("DetectorXDefaultSetup:");
      System.out.format("    nodeName         : %s %n", test.axis.getName());
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
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
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
      System.out.println("\nMOVE TO CENTER OF STAGE AND SET POSITION TO 0.0");
      // Initiate
      futOp = test.axis.moveRelative(VENDOR_OFFSET);
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
        logger.log(Level.SEVERE, null, ex);
        System.exit(1);
      }
      catch (CancellationException ex)
      {
        logger.log(Level.INFO, null, ex);
        System.exit(1);
      }

      // Set position to 0.0
      test.axis.setPosition(0.0);
      
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
      logger.log(Level.SEVERE, null, ex);
    }
  }

  private static void printUsage()
  {
    System.out.println(
    "USAGE: java -jar DetectorXDefaultSetup.jar "
    + "[<nodeName [String]>]");
  }
}
