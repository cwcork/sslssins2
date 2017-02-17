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
public class PLS85HydraAxisRemoteSetup
{
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisRemoteSetup.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private AxisArrayProxy array;
    private Axis axis;

    public PLS85HydraAxisRemoteSetup(String serverId)
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
        PLS85HydraAxisRemoteSetup app;
        
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

            app = new PLS85HydraAxisRemoteSetup(serverId);
            
            // Describe setup
            System.out.println("PLS85HydraAxisRemoteSetup:");
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
          "USAGE: java cxro.common.device.axis.PLS85AxisServerRemoteSetup "
          + "<objectName [String]>");
    }
}
