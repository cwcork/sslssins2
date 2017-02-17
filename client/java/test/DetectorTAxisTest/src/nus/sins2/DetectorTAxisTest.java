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
 * Test NUS.SINS2 DetectorT axis within ReflectometerStage via Sins2Instruments helper.
 * NOTE: Units for stages are mm for linear and degree for angular.
 * <p>
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public class DetectorTAxisTest
{
  private static final Logger logger = Logger.getLogger(DetectorTAxisTest.class.getName());

  static // static configuration
  {
    logger.setLevel(null);
  }
  //
  //instance fields
  private final Sins2Instruments instruments;
  private final Axis axis;

  public DetectorTAxisTest(String userDir)
  throws IOException, InterruptedException
  {
    this.instruments = new Sins2Instruments(userDir);
    this.axis = instruments.getDetectorT();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    DetectorTAxisTest test;
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

      test = new DetectorTAxisTest(System.getProperty("user.dir"));

      // Describe setup
      System.out.println("DetectorTAxisTest:");
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
      System.out.println("\nMOVE TO MIDPOINT, THEN BEGIN MOVE TESTS");
      double midpoint = (test.axis.getUpperLimitSoft() + test.axis.getLowerLimitSoft()) / 2.0;
      System.out.println("  Move to midpoint : " + midpoint);
      // Initiate
      futOp = test.axis.moveAbsolute(midpoint);
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

      // Do a series of absolute position moves
      //   Start at midpoint
      futOp = test.axis.moveAbsolute(midpoint);
      try
      {
        status = futOp.get();
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
      //   Oscillate about midpoint
      System.out.println("\nPerform a series of absolute position moves:");
      for (int i = 0; i < 10; i++)
      {
        System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
        if (i % 2 == 0)
        {
          futOp = test.axis.moveAbsolute(midpoint + 5.0);
          System.out.format("    %d: New Target       : midpoint + 5.0 %n%n", i);
        }
        else
        {
          futOp = test.axis.moveAbsolute(midpoint - 5.0);
          System.out.format("    %d: New Target       : midpoint - 5.0 %n%n", i);
        }
        try
        {
          status = futOp.get();
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
      }
      
      // Do a series of relative position moves
      //   Start at midpoint - 5.0
      futOp = test.axis.moveAbsolute(midpoint - 5.0);
      try
      {
        status = futOp.get();
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
      //   Oscillate about midpoint
      System.out.println("\nPerform a series of relative position moves:");
      for (int i = 0; i < 10; i++)
      {
        System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
        if (i % 2 == 0)
        {
          futOp = test.axis.moveRelative(+10.0);
          System.out.format("    %d: New Target       : current + 10.0 %n%n", i);
        }
        else
        {
          futOp = test.axis.moveRelative(-10.0);
          System.out.format("    %d: New Target       : current - 10.0 %n%n", i);
        }
        try
        {
          status = futOp.get();
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
      }
      
      // Do a series of target position moves
      //   Start at midpoint
      futOp = test.axis.moveAbsolute(midpoint);
      try
      {
        status = futOp.get();
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
      //   Oscillate about midpoint
      System.out.println("\nPerform a series of target position moves with long delay time:");
      for (int i = 0; i < 10; i++)
      {
        System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
        if (i % 2 == 0)
        {
          test.axis.setTarget(midpoint + 5.0);
        }
        else
        {
          test.axis.setTarget(midpoint - 5.0);
        }
        System.out.format("    %d: New Target       : %#f %n%n", i, test.axis.getTarget());
        Thread.sleep(2000);
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
        logger.log(Level.INFO, null, ex);
        System.exit(1);
      }
     
      //   Return to midpoint
      futOp = test.axis.moveAbsolute(midpoint);
      try
      {
        status = futOp.get();
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
      //   Oscillate about midpoint
      System.out.println("\nPerform a series of target position moves with short delay time:");
      for (int i = 0; i < 10; i++)
      {
        System.out.format("    %d: Current Position : %#f %n", i, test.axis.getPosition());
        if (i % 2 == 0)
        {
          test.axis.setTarget(midpoint + 5.0);
        }
        else
        {
          test.axis.setTarget(midpoint - 5.0);
        }
        System.out.format("    %d: New Target       : %#f %n%n", i, test.axis.getTarget());
        Thread.sleep(500);
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
        logger.log(Level.INFO, null, ex);
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
    "USAGE: java -jar DetectorTAxisTest.jar ");
  }
}
