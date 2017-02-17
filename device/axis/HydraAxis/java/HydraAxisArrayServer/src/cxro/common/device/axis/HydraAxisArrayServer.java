// @license
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HydraAxisArrayServer
extends Ice.Application
{
  private static final Logger LOGGER = Logger.getLogger(HydraAxisArrayServer.class.getName());
    //
  // instance
  private HydraComm comm;
  private HydraAxisArray array;

  @Override
  public int
  run(String[] args)
  {
    String objectName;
    String nodeName;
    String hostName;
    int arraySize;

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
        arraySize = Integer.parseInt(args[3]);
      }
      catch (Exception ex)
      {
        printUsage();
        return 1;
      }
    }

    try
    {
      this.comm = new HydraComm(hostName);
      this.array = new HydraAxisArray(nodeName, comm, arraySize);

            //BEGDEBUG
      // Describe setup
      System.out.println("HydraAxisArraySetup:");
      System.out.println("  nodeName       = " + array.getName());
      System.out.println("  hostName       = " + comm.getHostName());
      System.out.println("  arraySize      = " + array.getSize());

      int index = 0;
      for (HydraAxis a : this.array.getAxes())
      {
        System.out.format("  Axis %d, initial parameters: %n", index++);
        System.out.format("    nodeName         : %s %n", a.getName());
        System.out.format("    axisno           : %d %n", a.getAxisNumber());
        System.out.format("    scale            : %#f %n", a.getScale());
        System.out.format("    offset           : %#f %n", a.getOffset());
        System.out.format("    speed            : %#f %n", a.getSpeed());
        System.out.format("    accel            : %#f %n", a.getAcceleration());
        System.out.format("    upperLimitHard   : %#f %n", a.getUpperLimitHard());
        System.out.format("    upperLimitSoft   : %#f %n", a.getUpperLimitSoft());
        System.out.format("    lowerLimitSoft   : %#f %n", a.getLowerLimitSoft());
        System.out.format("    lowerLimitHard   : %#f %n", a.getLowerLimitHard());
        System.out.format("    isInitialized?   : %b %n", a.isInitialized());
        System.out.format("    ZeroOffset       : %#f %n", a.getOffset());
        System.out.format("    PositionRaw      : %#f %n", a.getPositionRaw());
        System.out.format("    Position         : %#f %n", a.getPosition());
        System.out.format("    At Upper Limit   : %b %n", a.getSwitches()[0]);
        System.out.format("    At Lower Limit   : %b %n", a.getSwitches()[1]);
      }
      //ENDDEBUG

      Ice.ObjectAdapter adapter;
      adapter = communicator().createObjectAdapter(objectName + "Adapter");
      adapter.add(new AxisArrayServant(adapter, array), communicator().stringToIdentity(objectName));
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
    HydraAxisArrayServer app = new HydraAxisArrayServer();
    int status = app.main("HydraAxisArrayServer", args, "config/HydraAxisArrayServer.config");
    System.exit(status);
  }

  private static void printUsage()
  {
    System.out.println(
    "USAGE: java -jar HydraAxisArrayServer.jar "
    + "<ice objectname [String]> <device nodename [String]> <Hydra ip-address [String]> <number of axes [int]");
  }

}
