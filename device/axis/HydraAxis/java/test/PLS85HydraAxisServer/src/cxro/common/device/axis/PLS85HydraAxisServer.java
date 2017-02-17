// **********************************************************************
//
// Copyright (c) 2003-2011 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PLS85HydraAxisServer
    extends Ice.Application
{
    private String nodeName;
    private String hostName;
    private int axisno;
    private HydraComm comm;
    private HydraAxis axis;

    @Override
    public int
    run(String[] args)
    {
        try
        {
            if(args.length != 3)
            {
                printUsage();
                return 1;
            }
            else
            {
                try
                {
                    nodeName = args[0];
                    hostName = args[1];
                    axisno = Integer.parseInt(args[2]);
                }
                catch (Exception ex)
                {
                    printUsage();
                    return 1;
                }
            }

            // Initialize axis
            comm = new HydraComm(hostName);
            axis = new HydraAxis(nodeName, comm, axisno);

            axis.setHasHome(false);
            axis.setHasLimits(true);
            axis.setHasAuxEncoder(false);
            axis.setHasIndex(false);

            axis.setAxisUnits("mm");
            axis.setLowerLimitHardRaw(-0.1);
            axis.setUpperLimitHardRaw(+50.1);
            
            axis.setScale(1.0);
            axis.setOffset(0.0);
            axis.setLowerLimitSoft(0.0);
            axis.setUpperLimitSoft(50.0);
            axis.setSpeed(10.0);
            axis.setAcceleration(100.0);
            axis.setInitializeSpeed(5.0);

            // Initialize adapter and respond to requests
            Ice.ObjectAdapter adapter;
            adapter = communicator().createObjectAdapter("PLS85HydraAxisAdapter");
            adapter.add(new AxisServant(axis), communicator().stringToIdentity("PLS85HydraAxis"));
            adapter.activate();
            communicator().waitForShutdown();
        }
        catch (IOException ex)
        {
            Logger.getLogger(PLS85HydraAxisServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return 0;
    }

    public static void
    main(String[] args)
    {
        PLS85HydraAxisServer app = new PLS85HydraAxisServer();
        int status = app.main("PLS85HydraAxisServerServer", args, "config.server");
        System.exit(status);
    }

    private static void printUsage()
    {
        System.err.println("Usage: " +
            "java -jar PLS85HydraAxisServer.jar "
            + "<axis name [String]> <ip address [String]> <axis number [int]>");
        }
}
