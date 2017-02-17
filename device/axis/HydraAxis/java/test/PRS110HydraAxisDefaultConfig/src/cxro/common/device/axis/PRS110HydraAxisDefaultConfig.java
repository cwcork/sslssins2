/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cxro.common.device.axis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Set default configs for miCos PRS110 HydraAxis stage.
 * Also saves config data to local file named 'PRS110Config.xml'
 *
 * @author cwcork
 */
public class PRS110HydraAxisDefaultConfig
extends HydraAxisDefaultConfig
{
  // Class fields
  private static final Logger logger = Logger.getLogger(PRS110HydraAxisDefaultConfig.class.getName());
  //
  // Instance fields

  public PRS110HydraAxisDefaultConfig(int axisno)
  {
    super("/cxro/test/device/axis/HydraAxis/PRS110/" + axisno, axisno);

    // Now set PRS110 configs
    HydraAxis axis = getAxis();

    axis.setHasHome(false);
    axis.setHasLimits(true);
    axis.setHasAuxEncoder(false);
    axis.setHasIndex(true);

    axis.setAxisUnits("deg");
    axis.setLowerLimitHardRaw(-360.1);
    axis.setUpperLimitHardRaw(+360.1);

    axis.setScale(1.0);
    axis.setOffset(0.0);
    axis.setLowerLimitSoft(-360.0);
    axis.setUpperLimitSoft(+360.0);
    axis.setInitializeSpeed(5.0);
    axis.setDefaultSpeed(40.0);
    axis.setDefaultAcceleration(400.0);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    PRS110HydraAxisDefaultConfig configs;
    String fileName = "PRS110DefaultConfig.xml";
    int axisno = 1;
    File outputFile;

    // 0. Turn on full logging
    Logger.getLogger("").setLevel(Level.ALL);
    Handler[] handlers = Logger.getLogger("").getHandlers();
    for (Handler handler : handlers)
    {
      handler.setLevel(Level.ALL);
    }

    // Get input parameters
    if (args.length != 1)
    {
      printUsage();
      System.exit(1);
    }
    else
    {
      try
      {
        axisno = Integer.parseInt(args[0]);
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, null, ex);
        printUsage();
        System.exit(1);
      }
    }

    try // Export the config data
    {
      configs = new PRS110HydraAxisDefaultConfig(axisno);
      outputFile = new File(fileName);

      configs.export(outputFile);

      // List configs
      HydraAxis axis = configs.getAxis();

      System.out.println("nodeName            : " + axis.getName());
      System.out.println("axisNumber          : " + axis.getAxisNumber());
      //
      System.out.println("axisUnits           : " + axis.getAxisUnits());
      System.out.println("scale               : " + axis.getScale());
      System.out.println("offsetRaw           : " + axis.getOffset());
      System.out.println("auxEncoderScale     : " + axis.getAuxEncoderScale());
      System.out.println("auxEncoderOffsetRaw : " + axis.getAuxEncoderOffset());
      //
      System.out.println("defaultAcceleration : " + axis.getDefaultAcceleration());
      System.out.println("defaultSpeed        : " + axis.getDefaultSpeed());
      System.out.println("hasAuxEncoder?      : " + axis.hasAuxEncoder());
      System.out.println("hasHome?            : " + axis.hasHome());
      System.out.println("hasLimits?          : " + axis.hasLimits());
      System.out.println("hasIndex?           : " + axis.hasIndex());
      System.out.println("initializeSpeed     : " + axis.getInitializeSpeed());
      System.out.println("upperLimitHard      : " + axis.getUpperLimitHard());
      System.out.println("UpperLimitSoft      : " + axis.getUpperLimitSoft());
      System.out.println("LowerLimitSoft      : " + axis.getLowerLimitSoft());
      System.out.println("LowerLimitHard      : " + axis.getLowerLimitHard());
    }
    catch (IOException | BackingStoreException ex)
    {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  private static void printUsage()
  {
    System.out.println(
    "USAGE: java -jar PRS110HydraAxisGetDefaultConfigs "
    + "<axisnumber [1..2]");
  }
}
