// @license
package nus.sins2.device.ftr201;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * NUS SINS.2 FilterStage Device parameter data.
 * <p>
 * For local data storage of device parameter data. 
 * <p>
 * The locator is the IPV4 address of the Moxa Modbus/TCP gateway.
 * The axisno is the axis number (0..15) for the IAI controller on the Modbus/RTU bus.
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public final class FilterStageDeviceParameters
{
  private static final Logger logger = Logger.getLogger(FilterStageDeviceParameters.class.getName());
  private final Preferences prefs;
  //
  private String locator = "192.168.1.12";
  private int axisno = 0;
  private double stroke = 150.0;

  public FilterStageDeviceParameters(Preferences prefs)
  {
    this.prefs = prefs;
    //
    //load current settings
    this.loadConfigs();
  } //load current settings

  //------------------------ PUBLIC  METHODS ----------------------------------
  public final Preferences getPrefs()
  {
    return prefs;
  }

  public final String getLocator()
  {
    return locator;
  }

  public final void setLocator(String locator)
  {
    this.locator = locator;
    
    // Save to devicePrefs
    try
    {
      prefs.put("locator", this.locator);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final int getAxisno()
  {
    return axisno;
  }

  public final void setAxisno(int axisno)
  {
    this.axisno = axisno;
    
    // Save to devicePrefs
    try
    {
      prefs.putInt("axisno", this.axisno);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final double getStroke()
  {
    return stroke;
  }

  public final void setStroke(double stroke)
  {
    this.stroke = stroke;
    
    // Save to devicePrefs
    try
    {
      prefs.putDouble("stroke", this.stroke);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  /**
   * Load Filters parameter data.
   */
  public final void loadConfigs()
  {
    this.locator = prefs.get("locator", locator);
    this.axisno = prefs.getInt("axisno", axisno);
    this.stroke = prefs.getDouble("stroke", stroke);
    
    // Resynchronize
    saveConfigs();
  }

  /**
   * Save Reflectometer Stage parameter data.
   */
  public final void saveConfigs()
  {
    try
    {
      // Save properties
      prefs.put("locator", this.locator);
      prefs.putInt("axisno", this.axisno);
      prefs.putDouble("stroke", this.stroke);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }
}
