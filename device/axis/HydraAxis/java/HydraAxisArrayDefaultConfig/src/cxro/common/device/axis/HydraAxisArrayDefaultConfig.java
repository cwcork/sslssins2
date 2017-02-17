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
public class HydraAxisArrayDefaultConfig
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(HydraAxisArrayDefaultConfig.class.getName());
    //
    // Instance fields
    private final String nodeName;
    private final Preferences prefs;
    private final HydraAxisArray array;

    public HydraAxisArrayDefaultConfig(String nodeName, int size)
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
        array = new HydraAxisArray(nodeName, size);

        //link to node
        prefs = Preferences.userRoot().node(nodeName);
    }

    public HydraAxisArray getArray()
    {
        return array;
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
        HydraAxisArrayDefaultConfig configs;
        String nodeName = "/cxro/common/device/axis/HydraAxisArray/default";
        String fileName = "HydraAxisArrayDefaultConfig.xml";
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
            configs = new HydraAxisArrayDefaultConfig(nodeName, 2);
            outputFile = new File(fileName);

            configs.export(outputFile);

            // Display configs
            for (HydraAxis a : configs.array.getAxes())
            {
                System.out.println("nodeName              : " + a.getName());
                System.out.println("  axisNumber          : " + a.getAxisNumber());
                System.out.println("");
                System.out.println("  axisUnits           : " + a.getAxisUnits());
                System.out.println("  scale               : " + a.getScale());
                System.out.println("  offsetRaw           : " + a.getOffset());
                System.out.println("  auxEncoderScale     : " + a.getAuxEncoderScale());
                System.out.println("  auxEncoderOffsetRaw : " + a.getAuxEncoderOffset());
                System.out.println("");
                System.out.println("  defaultAcceleration : " + a.getDefaultAcceleration());
                System.out.println("  defaultSpeed        : " + a.getDefaultSpeed());
                System.out.println("  hasAuxEncoder?      : " + a.hasAuxEncoder());
                System.out.println("  hasHome?            : " + a.hasHome());
                System.out.println("  hasLimits?          : " + a.hasLimits());
                System.out.println("  initializeSpeed     : " + a.getInitializeSpeed());
                System.out.println("  upperLimitHard      : " + a.getUpperLimitHard());
                System.out.println("  UpperLimitSoft      : " + a.getUpperLimitSoft());
                System.out.println("  LowerLimitSoft      : " + a.getLowerLimitSoft());
                System.out.println("  LowerLimitHard      : " + a.getLowerLimitHard());
                System.out.println("");
            }
        }
        catch (IOException | BackingStoreException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
          "USAGE: java -jar HydraAxisArrayDefaultConfig.jar ");
    }
}
