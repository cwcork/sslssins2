// @license
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author cwcork
 */
public class HydraAxisArray
implements AxisArray
{
  // Class fields
  private static final Logger LOGGER = Logger.getLogger(HydraAxisArray.class.getName());
  //
  // Instance fields
  private final String nodeName;
  private final Preferences prefs;
  private final HydraAxis[] axes;

  public HydraAxisArray(String nodeName, HydraComm comm, int size)
  throws IOException
  {
    // Cannot have more than 2 axes per Hydra
    if ((size <= 0) || (2 < size))
    {
      throw new IllegalArgumentException("size out of range [1..2]");
    }

    // Get link to preferences
    this.nodeName = nodeName;
    prefs = Preferences.userRoot().node(nodeName);

    // Allocate axes. If found, load current settings
    axes = new HydraAxis[size];
    for (int i = 0; i < size; i++)
    {
      axes[i] = new HydraAxis((nodeName + "/" + i), comm, i + 1);
    }

    // Load existing parameters
    loadConfigs();

    // Do setup specific to controller
    // --- nothing so far ---
    // Make sure configs are saved
    saveConfigs();
  }

  /**
   * This is a utility constructor to aid with generation of
   * a default configuration file. It is not intended for normal use.
   * @param nodeName
   * @param size
   */
  HydraAxisArray(String nodeName, int size)
  {
    // Cannot have more than 4 axes per Galil
    if ((size <= 0) || (2 < size))
    {
      throw new IllegalArgumentException("size out of range [1..2]");
    }

    // Get new link to preferences
    this.nodeName = nodeName;
    prefs = Preferences.userRoot().node(nodeName);

    // Allocate axes. If found, load current settings
    axes = new HydraAxis[size];
    for (int i = 0; i < size; i++)
    {
      axes[i] = new HydraAxis(nodeName + "/" + i, i + 1);
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
   * @throws java.io.IOException
   */
  @Override
  public void destroy()
  throws IOException
  { 
    // Delegate
    for (Axis a : axes)
    {
      a.destroy();
    }
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
  public final HydraAxis[] getAxes()
  {
    return axes.clone();
  }

  @Override
  public final HydraAxis getAxis(int index)
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
