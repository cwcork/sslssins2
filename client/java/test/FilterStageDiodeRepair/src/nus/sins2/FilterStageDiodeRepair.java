// @license
package nus.sins2;

import cxro.common.device.axis.Axis;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test NUS.SINS2 FTR201 stage via Sins2Instruments helper.
 * NOTE: CXRO standard units for stages are mm for linear and mRadian for angular.
 * <p>
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public class FilterStageDiodeRepair
{
  private static final Logger logger = Logger.getLogger(FilterStageDiodeRepair.class.getName());

  static // static configuration
  {
    logger.setLevel(null);
  }
  //
  //instance fields
  private final Sins2Instruments instruments;
  private final Axis axis;

  public FilterStageDiodeRepair(String userDir)
  throws IOException, InterruptedException
  {
    this.instruments = new Sins2Instruments(userDir);
    this.axis = instruments.getFilterStage();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    FilterStageDiodeRepair test;
    Future<Integer> futOp;

    // 0. Set log level
    Logger.getLogger("").setLevel(Level.INFO); // INFO only
//    Logger.getLogger("").setLevel(Level.ALL); // ALL levels
    Handler[] handlers = Logger.getLogger("").getHandlers();
    for (Handler handler : handlers)
    {
      handler.setLevel(Level.ALL);
    }

    // Do the tests
    try
    {
      int status;

      test = new FilterStageDiodeRepair(System.getProperty("user.dir"));

      // Describe setup
      System.out.println("FilterStageTest:");
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

      // Move to maximum position
      System.out.println("\nMOVE TO FULL EXTENSION");
      // Initiate
      futOp = test.axis.moveAbsolute(150.0);
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

      System.out.println("  After move");
      System.out.format("      isInitialized? : %b %n", test.axis.isInitialized());
      System.out.format("      ZeroOffset     : %#f %n", test.axis.getOffset());
      System.out.format("      PositionRaw    : %#f %n", test.axis.getPositionRaw());
      System.out.format("      Position       : %#f %n", test.axis.getPosition());
      System.out.format("      At Upper Limit : %b %n", test.axis.getSwitches()[0]);
      System.out.format("      At Lower Limit : %b %n", test.axis.getSwitches()[1]);

      // Done
      System.out.println("\nDONE");
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
    "USAGE: java -jar FilterStageTest.jar "
    + "[<nodeName [String]>]");
  }
}
