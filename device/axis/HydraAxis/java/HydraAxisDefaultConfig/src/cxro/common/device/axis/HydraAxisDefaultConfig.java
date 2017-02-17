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
 *
 * @author cwcork
 */
public class HydraAxisDefaultConfig
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(HydraAxisDefaultConfig.class.getName());
    //
    // Instance fields
    private final String nodeName;
    private final Preferences prefs;
    private final HydraAxis axis;

    public HydraAxisDefaultConfig(String nodeName, int axisno)
    {
        this.nodeName = nodeName;

        try // Clear current preferences
        {
            Preferences tmp = Preferences.userRoot().node(nodeName);
            tmp.removeNode();
            tmp.flush();
        }
        catch (BackingStoreException ex)
        {
            LOGGER.log(Level.WARNING, null, ex);
        }

        // Initialize the default configs
        axis = new HydraAxis(nodeName, axisno);

        //link to node
        prefs = Preferences.userRoot().node(nodeName);

        LOGGER.log(Level.INFO, "HydraAxisDefaultConfig ctor: {0}", nodeName);
    }

    public HydraAxis getAxis()
    {
        return axis;
    }

    /**
     * Exports from java preferences to an XML file.
     * <p/>
     * The xml will contain the node and all child nodes of this axis.
     * This is intended to be used mostly by admin tools for distribution
     * of configuration data.
     *
     * @param exportFile An xml file to hold the config data.
     * @return true if export was successful
     */
    public final boolean export(File exportFile)
      throws IOException, BackingStoreException
    {
        // Save all values to exportFile
        OutputStream fos = new FileOutputStream(exportFile);
        prefs.exportSubtree(fos);
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        HydraAxisDefaultConfig configs;
        String nodeName = "/cxro/common/device/axis/HydraAxis/default";
        String fileName = "HydraAxisDefaultConfig.xml";
        File outputFile;

        // 0. Turn on full logging
        Logger.getLogger("").setLevel(Level.ALL);
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            handlers[index].setLevel(Level.ALL);
        }

        try // Export the config data
        {
            configs = new HydraAxisDefaultConfig(nodeName, 1);
            outputFile = new File(fileName);

            configs.export(outputFile);

            // List configs
            System.out.println("nodeName            : " + configs.axis.getName());
            System.out.println("axisNumber          : " + configs.axis.getAxisNumber());
            //
            System.out.println("axisUnits           : " + configs.axis.getAxisUnits());
            System.out.println("scale               : " + configs.axis.getScale());
            System.out.println("offsetRaw           : " + configs.axis.getOffset());
            System.out.println("auxEncoderScale     : " + configs.axis.getAuxEncoderScale());
            System.out.println("auxEncoderOffsetRaw : " + configs.axis.getAuxEncoderOffset());
            //
            System.out.println("defaultAcceleration : " + configs.axis.getDefaultAcceleration());
            System.out.println("defaultSpeed        : " + configs.axis.getDefaultSpeed());
            System.out.println("hasAuxEncoder?      : " + configs.axis.hasAuxEncoder());
            System.out.println("hasHome?            : " + configs.axis.hasHome());
            System.out.println("hasLimits?          : " + configs.axis.hasLimits());
            System.out.println("initializeSpeed     : " + configs.axis.getInitializeSpeed());
            System.out.println("upperLimitHard      : " + configs.axis.getUpperLimitHard());
            System.out.println("UpperLimitSoft      : " + configs.axis.getUpperLimitSoft());
            System.out.println("LowerLimitSoft      : " + configs.axis.getLowerLimitSoft());
            System.out.println("LowerLimitHard      : " + configs.axis.getLowerLimitHard());
        }
        catch (IOException | BackingStoreException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
          "USAGE: java -jar HydraAxisDefaultConfig ");
    }
}
