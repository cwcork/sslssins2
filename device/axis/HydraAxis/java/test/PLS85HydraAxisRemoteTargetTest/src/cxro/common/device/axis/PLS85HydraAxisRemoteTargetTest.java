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
public class PLS85HydraAxisRemoteTargetTest
{
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisRemoteTargetTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private AxisArrayProxy array;
    private Axis axis;

    public PLS85HydraAxisRemoteTargetTest(String serverId)
      throws IOException, InterruptedException
    {
        this.array = new AxisArrayProxy(serverId);
        this.axis = array.getAxis(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PLS85HydraAxisRemoteTargetTest app;

        String serverId = "";
        double offset;
        double lowerLimit;
        double upperLimit;
        double midpoint;

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
                serverId = args[0];
            }
            catch (Exception ex)
            {
                printUsage();
                System.exit(1);
            }
        }

        //2. Do the setup
        try
        {
            int status = 0;

            app = new PLS85HydraAxisRemoteTargetTest(serverId);

            // Describe setup
            System.out.println("PLS85HydraAxisRemoteTargetTest:");
            System.out.println("\tserverId       = " + app.axis.getName());
            System.out.println("\tinitialOffset  = " + app.axis.getOffset());
            System.out.println("\tscale          = " + app.axis.getScale());
            System.out.println("\tlowerLimitHard = " + app.axis.getLowerLimitHard());
            System.out.println("\tupperLimitHard = " + app.axis.getUpperLimitHard());

            // Do simple preparation first
            System.out.println("Enable axis and make sure motor is stopped");
            app.axis.enable();
            stopOp = app.axis.stopMove();
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
            System.out.format("  isStopped? : %b %n", app.axis.isStopped());
            System.out.format("  isReady?   : %b %n", app.axis.isReady());

            // Get/Set position and offset
            System.out.println("Get position information");
            System.out.format("  offset    : %#f %n", app.axis.getOffset());
            System.out.format("  raw    pos: %#f %n", app.axis.getPositionRaw());
            System.out.format("  cooked pos: %#f %n", app.axis.getPosition());
            System.out.format("  speed     : %#f %n", app.axis.getSpeed());

            // INITIALIZE AXIS
            System.out.println("MAKE AXIS READY");
            System.out.println("  Before INITIALIZE");
            System.out.format("    isInitialized? : %b %n", app.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", app.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", app.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", app.axis.getPosition());
            System.out.format("    Upper Limit SW : %b %n", app.axis.getSwitches()[1]);
            System.out.format("    Lower Limit SW : %b %n", app.axis.getSwitches()[0]);

            System.out.println("  INITIALIZE");
            moveOp = app.axis.initialize();
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
                System.out.println("Cancellation Position: " + app.axis.getPosition() + app.axis.getAxisUnits());
                System.exit(1);
            }
            lowerLimit = app.axis.getPosition();
            System.out.println("    Axis Status: " + status);

            System.out.println("  After INITIALIZE");
            System.out.format("    isInitialized?   : %b %n", app.axis.isInitialized());
            System.out.format("    ZeroOffset       : %#f %n", app.axis.getOffset());
            System.out.format("    PositionRaw      : %#f %n", app.axis.getPositionRaw());
            System.out.format("    Position         : %#f %n", app.axis.getPosition());
            System.out.format("    Upper Limit SW   : %b %n", app.axis.getSwitches()[1]);
            System.out.format("    Lower Limit SW   : %b %n", app.axis.getSwitches()[0]);
            System.out.format("    Upper Limit Hard : %#f %n", app.axis.getUpperLimitHard());
            System.out.format("    Upper Limit Soft : %#f %n", app.axis.getUpperLimitSoft());
            System.out.format("    Lower Limit Soft : %#f %n", app.axis.getLowerLimitSoft());
            System.out.format("    Lower Limit Hard : %#f %n", app.axis.getLowerLimitHard());

            //move to midpoint
            System.out.println("Moving to origin at midpoint.");
            moveOp = app.axis.moveAbsolute(0.0);
            try
            {
                do
                {
                    System.out.format("  position = %#f  \r", app.axis.getPosition());
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
                System.out.println("Cancellation Position: " + app.axis.getPosition() + app.axis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);

            //Verify
            System.out.println("At origin");
            System.out.format("    isInitialized? : %b %n", app.axis.isInitialized());
            System.out.format("    ZeroOffset     : %#f %n", app.axis.getOffset());
            System.out.format("    PositionRaw    : %#f %n", app.axis.getPositionRaw());
            System.out.format("    Position       : %#f %n", app.axis.getPosition());
            System.out.format("    Upper Limit    : %b %n", app.axis.getSwitches()[1]);
            System.out.format("    Lower Limit    : %b %n", app.axis.getSwitches()[0]);


            // Do a series of target move tests
            System.out.println("\nPerform a series of target move tests");

            // Put target at current position
            app.axis.setTarget(0.0);
            System.out.format("  Current Target : %#f %n", app.axis.getTarget());

            // Try moves beyond soft limits
            System.out.println("\n  Attempt moves outside limits");
            status = app.axis.setTarget(100.0);
            System.out.format("    Attempt move to +100.0, result = %d %n", status);
            System.out.format("    Current Target : %#f %n", app.axis.getTarget());
            status = app.axis.setTarget(-100.0);
            System.out.format("    Attempt move to -100.0, result = %d %n", status);
            System.out.format("    Current Target : %#f %n", app.axis.getTarget());

            // Initiate blocking move first
            System.out.println("\n  Attempt target move while blocking move in progress");
            moveOp = app.axis.moveAbsolute(-5.0);
            Thread.sleep(100);
            status = app.axis.setTarget(+10.0);
            System.out.format("    Attempt to setTarget(+10.0) while moveAbsolute(-5.0) in progress; status = %d %n", status);
            System.out.format("    Current Target : %#f %n", app.axis.getTarget());
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
                System.out.println("Cancellation Position: " + app.axis.getPosition() + app.axis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);

            // Do a series of oscillating moves
            System.out.println("\n  Do a series of oscillating moves");
            for (int i = 0; i < 10; i++)
            {
                System.out.format("    %d: Current Position : %#f %n", i, app.axis.getPosition());
                if (i%2 == 0)
                {
                    app.axis.setTarget(+10.0);
                }
                else
                {
                    app.axis.setTarget(-10.0);
                }
                System.out.format("    %d: Current Target : %#f %n%n", i, app.axis.getTarget());
                Thread.sleep(1000);
            }
            // wait for final move to complete
            System.out.println("  Wait for final move to complete \n");
            try
            {
                do
                {
                    System.out.format("  position = %#f  \r", app.axis.getPosition());
                    Thread.sleep(100);
                }
                while (!app.axis.isReady());
                System.out.println("  Ready");
            }
            catch (CancellationException ex)
            {
                LOGGER.log(Level.INFO, null, ex);
                System.exit(1);
            }

            
            // leave at midpoint
            System.out.println("\nLeave at midpoint.");
            moveOp = app.axis.moveAbsolute(0.0);
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
                System.out.println("Cancellation Position: " + app.axis.getPosition() + app.axis.getAxisUnits());
                System.exit(1);
            }
            System.out.println("  Axis Status: " + status);

            // Done
            System.out.println("Done");
            app.array.destroy();
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
          "USAGE: java cxro.common.device.axis.PLS85HydraAxisRemoteTargetTest "
          + "<serverId [String]>");
    }
}
