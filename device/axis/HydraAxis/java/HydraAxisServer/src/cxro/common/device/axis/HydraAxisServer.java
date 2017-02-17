// @license
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HydraAxisServer
extends Ice.Application
{
  @Override
  public int
  run(String[] args)
  {
    String objectName;
    String nodeName;
    String hostName;
    int axisno;
    HydraComm comm;
    HydraAxis axis;

    try
    {
      if (args.length != 4)
      {
        printUsage();
        return 1;
      }
      else
      {
        try
        {
          objectName = args[0];
          nodeName = args[1];
          hostName = args[2];
          axisno = Integer.parseInt(args[3]);
        }
        catch (Exception ex)
        {
          printUsage();
          return 1;
        }
      }

      // Initialize axis, uses defaults and/or preferences for all settings.
      comm = new HydraComm(hostName);
      axis = new HydraAxis(nodeName, comm, axisno);

      // Initialize adapter and respond to requests
      Ice.ObjectAdapter adapter;
      adapter = communicator().createObjectAdapter(objectName + "Adapter");
      adapter.add(new AxisServant(axis), communicator().stringToIdentity(objectName));
      adapter.activate();
      communicator().waitForShutdown();
    }
    catch (IOException ex)
    {
      Logger.getLogger(HydraAxisServer.class.getName()).log(Level.SEVERE, null, ex);
      return 1;
    }
    return 0;
  }

  public static void
  main(String[] args)
  {
    HydraAxisServer app = new HydraAxisServer();
    int status = app.main("HydraAxisServer", args, "config/HydraAxisServer");
    System.exit(status);
  }

  private static void printUsage()
  {
    System.err.println("Usage: " + "USAGE: java -jar HydraAxisServer.jar "
                       + "<ice object name [String]> <device node name [String]> <Hydra ip address [String]> <Hydra axis number [int]>");
  }
}
