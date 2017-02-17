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
public class SimulatedAxisArrayProxyTest
{
    private static final Logger LOGGER = Logger.getLogger(SimulatedAxisArrayProxyTest.class.getName());

    static // static configuration
    {
        LOGGER.setLevel(null);
    }
    //
    //instance fields
    private AxisArrayProxy axisArray = null;
    private Axis[] axes;

    public SimulatedAxisArrayProxyTest(String axisArrayName)
      throws IOException, InterruptedException
    {
        axisArray = new AxisArrayProxy(axisArrayName);
        axes = axisArray.getAxes();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        SimulatedAxisArrayProxyTest test;
        String axisArrayName = "";

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
                axisArrayName = args[0];
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
            int index;
            int status = 0;
            Future<Integer> futOp;

            test = new SimulatedAxisArrayProxyTest(axisArrayName);

            // Describe setup
            System.out.format("SimulatedAxisArrayTest: %n");
            System.out.format("  name       : %s %n", test.axisArray.getName());
            System.out.format("  array size : %d %n", test.axisArray.getSize());

            index = 0;
            for (Axis a : test.axes)
            {
                System.out.format("  Axis %d, initial parameters: %n", index++);
                System.out.format("    scale            : %#f %n", a.getScale());
                System.out.format("    offset           : %#f %n", a.getOffset());
                System.out.format("    speed            : %#f %n", a.getSpeed());
                System.out.format("    accel            : %#f %n", a.getAcceleration());
                System.out.format("    raw    pos       : %#f %n", a.getPositionRaw());
                System.out.format("    cooked pos       : %#f %n", a.getPosition());
                System.out.format("    At Upper Limit   : %b %n", a.getSwitches()[0]);
                System.out.format("    At Lower Limit   : %b %n", a.getSwitches()[1]);
            }

            // INITIALIZE AXES
            System.out.println("\nINITIALIZE AXES");
            System.out.println("  Before initialize");
            index = 0;
            for (Axis a : test.axes)
            {
                System.out.format("    Axis %d %n", index++);
                System.out.format("      isInitialized? : %b %n", a.isInitialized());
                System.out.format("      ZeroOffset     : %#f %n", a.getOffset());
                System.out.format("      PositionRaw    : %#f %n", a.getPositionRaw());
                System.out.format("      Position       : %#f %n", a.getPosition());
                System.out.format("      At Upper Limit : %b %n", a.getSwitches()[0]);
                System.out.format("      At Lower Limit : %b %n", a.getSwitches()[1]);
            }

            System.out.println("  Initialize");
            index = 0;
            for (Axis a : test.axes)
            {
                futOp = a.initialize();
                try
                {
                    status = (Integer) futOp.get();
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
                System.out.format("    Axis %d Status: %d %n", index++, status);
            }

            System.out.println("  After initialize");
            index = 0;
            for (Axis a : test.axes)
            {
                System.out.format("    Axis %d %n", index++);
                System.out.format("      isInitialized? : %b %n", a.isInitialized());
                System.out.format("      ZeroOffset     : %#f %n", a.getOffset());
                System.out.format("      PositionRaw    : %#f %n", a.getPositionRaw());
                System.out.format("      Position       : %#f %n", a.getPosition());
                System.out.format("      Upper Limit Hard : %#f %n", a.getUpperLimitHard());
                System.out.format("      Upper Limit Soft : %#f %n", a.getUpperLimitSoft());
                System.out.format("      Lower Limit Soft : %#f %n", a.getLowerLimitSoft());
                System.out.format("      Lower Limit Hard : %#f %n", a.getLowerLimitHard());
            }

            // Leave at midpoint
            System.out.println("\nLEAVE AXES AT MIDPOINT");
            System.out.println("  Move to midpoint ...");
            double midpoint;
            index = 0;
            for (Axis a : test.axes)
            {
                System.out.format("    Axis %d %n", index++);

                midpoint = (a.getUpperLimitSoft() + a.getLowerLimitSoft()) / 2.0;
                System.out.format("      midpoint     : %#f %n", midpoint);

                futOp = a.moveAbsolute(midpoint);
                try
                {
                    status = (Integer) futOp.get();
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
                System.out.format("      move status  : %d %n", status);
                System.out.format("      PositionRaw  : %#f %n", a.getPositionRaw());
                System.out.format("      Position     : %#f %n", a.getPosition());
            }

            // Done
            System.out.println("\nDONE");
            test.axisArray.destroy();
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
          "USAGE: java cxro.common.device.axis.SimulatedAxisArrayTest "
          + "<axisArray name [String]>");
    }
}
