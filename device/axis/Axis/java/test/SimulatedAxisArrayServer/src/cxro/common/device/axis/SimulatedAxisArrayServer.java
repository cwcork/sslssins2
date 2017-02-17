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


public class SimulatedAxisArrayServer
    extends Ice.Application
{
    private static final Logger LOGGER = Logger.getLogger(SimulatedAxisArray.class.getName());

    AxisArray axes;

    @Override
    public int
    run(String[] args)
    {
        String arrayNodeName = "";
        int arraySize;

        if (args.length != 2)
        {
            printUsage();
            return 1;
        }
        else
        {
            try
            {
                arrayNodeName = args[0];
                arraySize = Integer.parseInt(args[1]);
            }
            catch (Exception ex)
            {
                printUsage();
                return 1;
            }
        }

        try
        {
            axes = new SimulatedAxisArray(arrayNodeName, arraySize);

            Ice.ObjectAdapter adapter;
            adapter = communicator().createObjectAdapter("SimulatedAxisArrayAdapter");
            adapter.add(new AxisArrayServant(adapter, axes), communicator().stringToIdentity("SimulatedAxisArray"));
            adapter.activate();
            communicator().waitForShutdown();
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
            return 1;
        }
        return 0;
    }

    public static void
    main(String[] args)
    {
        SimulatedAxisArrayServer app = new SimulatedAxisArrayServer();
        int status = app.main("SimulatedAxisArrayServer", args, "config/SimulatedAxisArray");
        System.exit(status);
    }

    private static void printUsage()
    {
        System.out.println(
          "USAGE: java cxro.common.device.axis.SimulatedAxisArrayServer "
          + "<axisArray nodeName [String]> <size of array [int]");
    }

}
