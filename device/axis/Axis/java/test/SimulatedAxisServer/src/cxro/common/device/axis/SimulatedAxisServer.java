// **********************************************************************
//
// Copyright (c) 2003-2011 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
package cxro.common.device.axis;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SimulatedAxisServer extends Ice.Application
{
    Axis axis;

    @Override
    public int
    run(String[] args)
    {
        try
        {
            if(args.length > 0)
            {
                System.err.println(appName() + ": too many arguments");
                return 1;
            }

            axis = new SimulatedAxis("cxro.common.device.axis.simulated");

            Ice.ObjectAdapter adapter;
            adapter = communicator().createObjectAdapter("SimulatedAxisAdapter");
            adapter.add(new AxisServant(axis), communicator().stringToIdentity("simulatedAxis"));
            adapter.activate();
            communicator().waitForShutdown();
        }
        catch (IOException ex)
        {
            Logger.getLogger(SimulatedAxisServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return 0;
    }

    public static void
    main(String[] args)
    {
        SimulatedAxisServer app = new SimulatedAxisServer();
        int status = app.main("SimulatedAxisServer", args, "config/SimulatedAxis");
        System.exit(status);
    }
}
