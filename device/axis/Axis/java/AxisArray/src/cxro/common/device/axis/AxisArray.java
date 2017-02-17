// @license
package cxro.common.device.axis;

import java.io.IOException;

/**
 * Interface for N Dimensional Devices
 * <p>
 * @author Carl Cork
 */
public interface AxisArray
{
  /**
   * Cleanup system resources before exit.
   * Needed when using system resources that are not normally handled by Java
   * garbage collector.
   * <p>
   * @throws IOException
   */
  void destroy()
  throws IOException;

  /**
   * Get device name
   * <p>
   * @return DeviceName
   */
  String getName();

  /**
   * Get number of axes
   * <p>
   * @return Number of axes
   */
  int getSize();

  /**
   * Get array of axes
   * <p>
   * @return Array of axis objects
   */
  Axis[] getAxes();

  /**
   * Get axis at specified index.
   *
   * @param index [0..(size-1)]
   * @return Axis object for index
   */
  Axis getAxis(int index);

  /**
   * Read configuration data for this device.
   * This is a DEEP load .. it iterates through the associated axes.
   */
  void loadConfigs();

  /**
   * Write configuration data for this device.
   * This is a DEEP save .. it iterates through the associated axes.
   */
  void saveConfigs();
}
