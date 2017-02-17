// @license
package nus.sins2.device.reflectometer;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Reflectometer Stage Device parameter data.
 * <p>
 * For local data storage of device parameter data. 
 * <p>
 * The controllers are indexed 0..2 (2 axes per controller) and the axes are indexed 0..5, where:<br>
 * axis 0 = maskX<br>
 * axis 1 = maskY<br>
 * axis 2 = maskZ<br>
 * axis 3 = detectorX<br>
 * axis 4 = maskT<br>
 * axis 5 = detectorT<br>
 */
public final class ReflectometerStageDeviceParameters
{
  private static final Logger logger = Logger.getLogger(ReflectometerStageDeviceParameters.class.getName());
  private final Preferences prefs;
  //
  private String[] locators = {"192.168.1.21", "192.168.1.22", "192.168.1.23"};
  private int[] controllerIndexes = {0, 1, 0, 1, 2, 2};
  private int[] axisIndexes = {1, 1, 2, 2, 2, 1};  

  public ReflectometerStageDeviceParameters(Preferences prefs)
  {
    this.prefs = prefs;
    //load current settings
    this.loadConfigs();
  } //load current settings

  //------------------------ PUBLIC  METHODS ----------------------------------
  public final Preferences getPrefs()
  {
    return prefs;
  }

  public final String[] getLocators()
  {
    return locators.clone();
  }

  public final void setLocators(String[] locators)
  {
    if (locators.length != 3)
    {
      throw new IllegalArgumentException("locators length must be = 3.");
    }
    this.locators = locators.clone();
    
    // Save to devicePrefs
    try
    {
      prefs.put("locator.0", this.locators[0]);
      prefs.put("locator.1", this.locators[1]);
      prefs.put("locator.2", this.locators[2]);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final int[] getControllerIndexes()
  {
    return Arrays.copyOf(controllerIndexes, 6);
  }

  public final void setControllerIndexes(int[] controllerIndexes)
  {
    if (controllerIndexes.length != 6)
    {
      throw new IllegalArgumentException("controllerIndexes length must be = 6.");
    }
    this.controllerIndexes = Arrays.copyOf(controllerIndexes, 6);
    
    // Save to devicePrefs
    try
    {
      prefs.putInt("controllerIndex.maskX", this.controllerIndexes[0]);
      prefs.putInt("controllerIndex.maskY", this.controllerIndexes[1]);
      prefs.putInt("controllerIndex.maskZ", this.controllerIndexes[2]);
      prefs.putInt("controllerIndex.detectorX", this.controllerIndexes[3]);
      prefs.putInt("controllerIndex.maskT", this.controllerIndexes[4]);
      prefs.putInt("controllerIndex.detectorT", this.controllerIndexes[5]);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final int[] getAxisIndexes()
  {
    return Arrays.copyOf(axisIndexes, 6);
  }

  public final void setAxisIndexes(int[] axisIndexes)
  {
    if (axisIndexes.length != 6)
    {
      throw new IllegalArgumentException("axisIndexes length must be = 6.");
    }
    this.axisIndexes = Arrays.copyOf(axisIndexes, 6);
    
    // Save to devicePrefs
    try
    {
      prefs.putInt("axisIndex.maskX", this.axisIndexes[0]);
      prefs.putInt("axisIndex.maskY", this.axisIndexes[1]);
      prefs.putInt("axisIndex.maskZ", this.axisIndexes[2]);
      prefs.putInt("axisIndex.detectorX", this.axisIndexes[3]);
      prefs.putInt("axisIndex.maskT", this.axisIndexes[4]);
      prefs.putInt("axisIndex.detectorT", this.axisIndexes[5]);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  /**
   * Load Reflectometer Stage parameter data.
   */
  public final void loadConfigs()
  {
    this.locators[0] = prefs.get("locator.0", locators[0]);
    this.locators[1] = prefs.get("locator.1", locators[1]);
    this.locators[2] = prefs.get("locator.2", locators[2]);
    this.controllerIndexes[0] = prefs.getInt("controllerIndex.maskX", controllerIndexes[0]);
    this.controllerIndexes[1] = prefs.getInt("controllerIndex.maskY", controllerIndexes[1]);
    this.controllerIndexes[2] = prefs.getInt("controllerIndex.maskZ", controllerIndexes[2]);
    this.controllerIndexes[3] = prefs.getInt("controllerIndex.detectorX", controllerIndexes[3]);
    this.controllerIndexes[4] = prefs.getInt("controllerIndex.maskT", controllerIndexes[4]);
    this.controllerIndexes[5] = prefs.getInt("controllerIndex.detectorT", controllerIndexes[5]);
    this.axisIndexes[0] = prefs.getInt("axisIndex.maskX", axisIndexes[0]);
    this.axisIndexes[1] = prefs.getInt("axisIndex.maskY", axisIndexes[1]);
    this.axisIndexes[2] = prefs.getInt("axisIndex.maskZ", axisIndexes[2]);
    this.axisIndexes[3] = prefs.getInt("axisIndex.detectorX", axisIndexes[3]);
    this.axisIndexes[4] = prefs.getInt("axisIndex.maskT", axisIndexes[4]);
    this.axisIndexes[5] = prefs.getInt("axisIndex.detectorT", axisIndexes[5]);
    
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
      prefs.put("locator.0", this.locators[0]);
      prefs.put("locator.1", this.locators[1]);
      prefs.put("locator.2", this.locators[2]);
      prefs.putInt("controllerIndex.maskX", this.controllerIndexes[0]);
      prefs.putInt("controllerIndex.maskY", this.controllerIndexes[1]);
      prefs.putInt("controllerIndex.maskZ", this.controllerIndexes[2]);
      prefs.putInt("controllerIndex.detectorX", this.controllerIndexes[3]);
      prefs.putInt("controllerIndex.maskT", this.controllerIndexes[4]);
      prefs.putInt("controllerIndex.detectorT", this.controllerIndexes[5]);
      prefs.putInt("axisIndex.maskX", this.axisIndexes[0]);
      prefs.putInt("axisIndex.maskY", this.axisIndexes[1]);
      prefs.putInt("axisIndex.maskZ", this.axisIndexes[2]);
      prefs.putInt("axisIndex.detectorX", this.axisIndexes[3]);
      prefs.putInt("axisIndex.maskT", this.axisIndexes[4]);
      prefs.putInt("axisIndex.detectorT", this.axisIndexes[5]);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }
}
