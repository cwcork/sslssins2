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
 * Set default configs for Micos PLS85 HydraAxis stage.
 * Also saves config data to local file named 'PLS85Config.xml'
 * @author cwcork
 */
public class PLS85HydraAxisDefaultConfig
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(PLS85HydraAxisDefaultConfig.class.getName());
    //
    // Instance fields
    private final String nodeName;
    private final Preferences prefs;

    public PLS85HydraAxisDefaultConfig(String nodeName)
    {
        this.nodeName = nodeName;

        //link to node
        prefs = Preferences.userRoot().node(nodeName);

        // Initialize the default configs
        HydraAxisDefaultConfig config = new HydraAxisDefaultConfig(nodeName, 1);
        HydraAxis axis = config.getAxis();

        // Now set PLS85 configs
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
        axis.setInitializeSpeed(5.0);
        axis.setDefaultSpeed(10.0);
        axis.setDefaultAcceleration(100.0);
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
        PLS85HydraAxisDefaultConfig configs;
        String nodeName = "";
        int axisno;

        String fileName = "PLS85DefaultConfig.xml";
        File outputFile;

        // 0. Turn on full logging
        Logger.getLogger("").setLevel(Level.ALL);
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            handlers[index].setLevel(Level.ALL);
        }

        // Get input parameters
            if(args.length != 2)
            {
                printUsage();
                System.exit(1);
            }
            else
            {
                try
                {
                    nodeName = args[0];
                    axisno = Integer.parseInt(args[1]);
                }
                catch (Exception ex)
                {
                    printUsage();
                    System.exit(1);
                }
            }

        try // Export the config data
        {
            configs = new PLS85HydraAxisDefaultConfig(nodeName);
            outputFile = new File(fileName);

            configs.export(outputFile);
        }
        catch (IOException | BackingStoreException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private static void printUsage()
    {
        System.out.println(
          "USAGE: java -jar HydraAxisGetDefaultConfigs ");
    }
}
