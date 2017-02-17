/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cxro.common.device.axis;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author cwcork
 */
public class SimulatedAxisArray
    implements AxisArray
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(SimulatedAxisArray.class.getName());
    //
    // Instance fields
    private final String nodeName;
    private final Preferences prefs;
    private final Axis[] axes;

    public SimulatedAxisArray(String nodeName, int size)
      throws IOException
    {
        // Check for array limits. Simulated has no upper limit
        if (size <= 0)
        {
            throw new IllegalArgumentException("size out of range [1..n]");
        }

        // Get link to preferences
        this.nodeName = nodeName;
        this.prefs = Preferences.userRoot().node(nodeName);

        // Allocate axes, use default config parameters
        axes = new Axis[size];
        for (int i = 0; i < size; i++)
        {
            axes[i] = new SimulatedAxis(nodeName + "/" + i);
        }

        // Load existing parameters
        loadConfigs();

        // Do setup specific to controller
        // --- nothing so far ---

        // Make sure configs are saved
        saveConfigs();
    }

    /**
     * Do any cleanup before leaving to garbage collector.
     * Needed by Proxy class for clean exit by Ice.
     */
    @Override
    public void destroy()
    {
        // For compatibility with SimulatedAxisArrayProxy
        // Needed by Ice
    }

    @Override
    public final String getName()
    {
        return nodeName;
    }

    @Override
    public final int getSize()
    {
        return axes.length;
    }

    @Override
    public final Axis[] getAxes()
    {
        return axes.clone();
    }

    @Override
    public final Axis getAxis(int index)
    {
        return axes[index];
    }

    @Override
    public final void loadConfigs()
    {
        if (axes != null)
        {
            for (Axis a : axes)
            {
                a.loadConfigs();
            }
        }
    }

    @Override
    public final void saveConfigs()
    {
        if (axes != null)
        {
            for (Axis a : axes)
            {
                a.saveConfigs();
            }
        }
    }
}
