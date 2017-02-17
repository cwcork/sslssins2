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
public class HydraCommTest_PLS85
{
    // static fields
    private static final Logger fLogger = Logger.getLogger(HydraCommTest_PLS85.class.getName());

    static // static configuration
    {
        fLogger.setLevel(null);
    }
    //
    //instance fields
    private HydraComm fPort = null;

    public HydraCommTest_PLS85(String hostName)
      throws IOException
    {
        fPort = new HydraComm(hostName);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        HydraCommTest_PLS85 test;
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
        if (args.length < 2)
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
        try
        {
            String cmd;
            String res = null;

            test = new HydraCommTest_PLS85(hostName);

            // Do simple cleanup first
            System.out.println("Ctrl-C : Stop All Motors");
            test.fPort.snd("\0x3"); // Ctrl-C stop all motors

            // Do a read with no available data. Should fail immediately
            System.out.println("Do a read with no available data");
            try
            {
                res = test.fPort.rcv(100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex)
            {
                fLogger.log(Level.INFO, "Timeout Expected", ex);
            }
            if (res == null)
            {
                System.out.println("rsp = null");
            }
            else
            {
                System.out.println(res);
            }

            cmd = String.format("%d nclear \n", axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            // do req/rsp sequence that is not thread safe
            cmd = String.format("%d nidentify \n", axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);
            res = test.fPort.rcv(1, TimeUnit.SECONDS);
            System.out.println(res);

            // do rest of commands in threadsafe fashion
            cmd = String.format("%d getserialno \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%d nversion \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%d ngsp \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);


            cmd = String.format("%d getvarint \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%d %d setvarint \n", 1, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            cmd = String.format("%d getvarint \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);


            cmd = String.format("%d gnv \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%#f %d snv \n", 0.3, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            cmd = String.format("%d gnv \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);


            cmd = String.format("%d gna \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%#f %d sna \n", 10.0, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            cmd = String.format("%d gna \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");


            // restore settings
            System.out.println("Restore speed and acceleration settings.");
            cmd = String.format("%#f %d snv \n", 10.0, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            cmd = String.format("%#f %d sna \n", 200.0, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);

            cmd = String.format("%d gnv \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%d gna \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");


            // get initial position and limit switch state
            cmd = String.format("%d np \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);

            cmd = String.format("%d getswst \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");

            // find lower limit
            cmd = String.format("%d ncal \n", axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);
            cmd = String.format("%d nst \n", axisno);
            do
            {
                res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            }
            while ((Integer.parseInt(res.trim()) & 0x01) != 0);
            cmd = String.format("%d np \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");

            // find upper limit
            cmd = String.format("%d nrm \n", axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);
            cmd = String.format("%d nst \n", axisno);
            do
            {
                res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            }
            while ((Integer.parseInt(res.trim()) & 0x01) != 0);
            cmd = String.format("%d np \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");

            // display limits
            cmd = String.format("%d getnlimit \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");


            // move to mid position
            cmd = String.format("%#f %d nm \n", 25.0, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);
            cmd = String.format("%d nst \n", axisno);
            do
            {
                res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            }
            while ((Integer.parseInt(res.trim()) & 0x01) != 0);
            cmd = String.format("%d np \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");

            cmd = String.format("%#f %d nr \n", 1.0, axisno);
            System.out.print(cmd);
            test.fPort.snd(cmd);
            cmd = String.format("%d nst \n", axisno);
            do
            {
                res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            }
            while ((Integer.parseInt(res.trim()) & 0x01) != 0);
            cmd = String.format("%d np \n", axisno);
            System.out.print(cmd);
            res = test.fPort.sar(cmd, 1, TimeUnit.SECONDS);
            System.out.println(res);
            System.out.println("");

            // Donecomm
            test.fPort.close();
            System.exit(0);
        }
        catch (InterruptedException ex)
        {
            fLogger.log(Level.SEVERE, null, ex);
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
    }//printUsage
}
