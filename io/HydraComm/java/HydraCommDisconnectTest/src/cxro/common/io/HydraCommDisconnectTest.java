// @license
package cxro.common.io;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cwcork
 */
public class HydraCommDisconnectTest
{
    // static fields
    private static final Logger fLogger = Logger.getLogger(HydraCommDisconnectTest.class.getName());

    static // static configuration
    {
        fLogger.setLevel(null);
    }
    //
    //instance fields
    private HydraComm fPort = null;

    public HydraCommDisconnectTest(String hostName)
        throws IOException
    {
        fPort = new HydraComm(hostName);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        HydraCommDisconnectTest test;
        String hostName = "192.168.1.11";
        int axisno = 1;

        // 0. Turn on full logging
        Logger.getLogger("").setLevel(Level.ALL);
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            handlers[index].setLevel(Level.ALL);
        }

        // Get the parameters
        if (args.length != 2)
        {
            printUsage();
            System.exit(1);
        }
        else
        {
            try
            {
                hostName = args[0];
                axisno = Integer.parseInt(args[1]);
            }
            catch (Exception ex)
            {
                printUsage();
                System.exit(1);
            }
        }

        // Do the tests
        String cmd;
        String res;
        try
        {
            test = new HydraCommDisconnectTest(hostName);

            // Do continuous loop test
            System.out.println("Loop on simple status test");
            cmd = String.format("%d nst \n", axisno);
            for (int i = 0; i < 1000; i++)
            {
                try
                {
                    
                    test.fPort.snd(cmd);
                    res = test.fPort.rcv(500, TimeUnit.MILLISECONDS);
                    System.out.println("cmd: " + cmd + " ; res: " + res);

                    res = test.fPort.sar(cmd, 500, TimeUnit.MILLISECONDS);
                    System.out.println("cmd: " + cmd + " ; res: " + res);
                    Thread.sleep(500);
                }
                catch (InterruptedException ex)
                {
                    fLogger.log(Level.INFO, "Timeout Expected", ex);
                }
                catch (IOException ex)
                {
                    fLogger.log(Level.SEVERE, null, ex);
                }
            }
            // Donecomm
            test.fPort.close();
            System.exit(0);
        }
        catch (IOException ex)
        {
            fLogger.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
            "java cxro.io.HydraCommTest_PLS85 "
            + "<IP hostName> <Hydra axis number>");
    }
}
