// &license
package nus.sins2.device.reflectometer;

import cxro.common.device.axis.Axis;
import cxro.common.device.axis.HydraAxis;
import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Stage controller object for NUS SINS2 Reflectometer.
 * <p>
 * This is a 6-axis PI MiCos system that is controlled by 3 Hydra motion controllers.
 * <p>
 * The controllers are indexed 0..2 (2 axes per controller) and the axes are indexed 0..5, where:<br>
 * axis 0 = maskX<br>
 * axis 1 = maskY<br>
 * axis 2 = maskZ<br>
 * axis 3 = detectorX<br>
 * axis 4 = maskT<br>
 * axis 5 = detectorT<br>
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public class ReflectometerStage
{
  // static fields
  private static final Logger logger = Logger.getLogger(ReflectometerStage.class.getName());

  private static final int  MASK_X = 0;
  private static final int  MASK_Y = 1;
  private static final int  MASK_Z = 2;
  private static final int  DETECTOR_X = 3;
  private static final int  MASK_T = 4;
  private static final int  DETECTOR_T = 5;
  private static final String DEFAULT_NODENAME = "/nus/sins2/device/reflectometer/stage";

  // Static methods
  public static String getDefaultNodename()
  {
    return DEFAULT_NODENAME;
  }

  //instance fields
  private final String nodeName;
  private final Preferences devicePrefs;
  private final ReflectometerStageDeviceParameters deviceParams;
  
  private final HydraComm[] comms;
  private final HydraAxis[] axes;

  // --------------------- Constructors ------------------------------------------------------------
  /**
   * Full constructor for NUS SINS2 Reflectometer stage.
   * <p>
   * Use specified parameter configuration file (Preferences XML file).
   * @param nodeName  configuration node name based on java.util.prefs.
   */
  public ReflectometerStage(String nodeName)
  {
    this.nodeName = nodeName;
    this.devicePrefs = Preferences.userRoot().node(nodeName);
    this.deviceParams = new ReflectometerStageDeviceParameters(this.devicePrefs);
    
    // Set up arrays
    this.comms = new HydraComm[3];
    this.axes = new HydraAxis[6];
    
    // Defer connections until client requests
  }
  
  /**
   * Zero argument constructor.
   * <p>
   * Use default configuration nodeName and locators from the configuration.
   */
  public ReflectometerStage() 
  {
    this(DEFAULT_NODENAME);
  }
  
  public synchronized void disconnect()
  {
    // release resources
    for (int i = 0; i < axes.length; i++)
    {
      if (axes[i] != null)
      {
        try
        {
          axes[i].destroy();
        }
        catch (IOException ignore)
        {
          // ignore exception
        }
        axes[i] = null;
      }
    }
    
    for (int i = 0; i < comms.length; i++)
    {
      if (comms[i] != null)
      {
        try
        {
          comms[i].close();
        }
        catch (IOException ignore)
        {
          // ignore exception
        }
      comms[i]  = null;
      }
    }
  }

  public synchronized Axis getMaskX()
  {
    if (axes[MASK_X] == null)
    {
      logger.info("Attempting to connect to MaskX Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[MASK_X];
      int axisIndex = deviceParams.getAxisIndexes()[MASK_X];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/mask_x", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("mm");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setLowerLimitHardRaw(-0.1);
        axis.setUpperLimitHardRaw(+150.1);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(3.0);
        axis.setDefaultSpeed(3.0);
        axis.setDefaultAcceleration(200.0);

        axis.enable();
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        axes[MASK_X] = axis;
        logger.info("... MaskX Stage connected");
      }
      catch (IOException ex)
      {
        axes[MASK_X] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[MASK_X];
  }

  public synchronized Axis getMaskY()
  {
    if (axes[MASK_Y] == null)
    {
      logger.info("Attempting to connect to MaskY Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[MASK_Y];
      int axisIndex = deviceParams.getAxisIndexes()[MASK_Y];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/mask_y", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("mm");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setLowerLimitHardRaw(-0.1);
        axis.setUpperLimitHardRaw(+150.1);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(3.0);
        axis.setDefaultSpeed(3.0);
        axis.setDefaultAcceleration(200.0);

        axis.enable();
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        axes[MASK_Y] = axis;
        logger.info("... MaskY Stage connected");
      }
      catch (IOException ex)
      {
        axes[MASK_Y] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[MASK_Y];
  }

  public synchronized Axis getMaskZ()
  {
    if (axes[MASK_Z] == null)
    {
      logger.info("Attempting to connect to MaskZ Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[MASK_Z];
      int axisIndex = deviceParams.getAxisIndexes()[MASK_Z];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/mask_z", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("mm");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setLowerLimitHardRaw(-0.1);
        axis.setUpperLimitHardRaw(+60.1);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(3.0);
        axis.setDefaultSpeed(3.0);
        axis.setDefaultAcceleration(200.0);

        axis.enable();
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        axes[MASK_Z] = axis;
        logger.info("... MaskZ Stage connected");
      }
      catch (IOException ex)
      {
        axes[MASK_Z] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[MASK_Z];
  }

  public synchronized Axis getDetectorX()
  {
    if (axes[DETECTOR_X] == null)
    {
      logger.info("Attempting to connect to DetectorX Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[DETECTOR_X];
      int axisIndex = deviceParams.getAxisIndexes()[DETECTOR_X];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/detector_x", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("mm");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(false);

        axis.setLowerLimitHardRaw(-0.1);
        axis.setUpperLimitHardRaw(+150.1);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(3.0);
        axis.setDefaultSpeed(3.0);
        axis.setDefaultAcceleration(200.0);

        axis.enable();
        axis.setInitialized(false);   // must be done because axis is open loop stepper
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
                
        // success
        axes[DETECTOR_X] = axis;
        logger.info("... DetectorX Stage connected");
      }
      catch (IOException ex)
      {
        axes[DETECTOR_X] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[DETECTOR_X];
  }

  public synchronized Axis getMaskT()
  {
    if (axes[MASK_T] == null)
    {
      logger.info("Attempting to connect to MaskT Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[MASK_T];
      int axisIndex = deviceParams.getAxisIndexes()[MASK_T];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/mask_t", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("deg");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(true);

        axis.setLowerLimitHardRaw(-153.6);
        axis.setUpperLimitHardRaw(+31.7);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(1.0);
        axis.setDefaultSpeed(1.0);
        axis.setDefaultAcceleration(10.0);

        axis.enable();
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        axes[MASK_T] = axis;
        logger.info("... MaskT Stage connected");
      }
      catch (IOException ex)
      {
        axes[MASK_T] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[MASK_T];
  }

  public synchronized Axis getDetectorT()
  {
    if (axes[DETECTOR_T] == null)
    {
      logger.info("Attempting to connect to DetectorT Stage ...");
      int controllerIndex = deviceParams.getControllerIndexes()[DETECTOR_T];
      int axisIndex = deviceParams.getAxisIndexes()[DETECTOR_T];

      // Get comm
      if (comms[controllerIndex] == null)
      {
        try
        {
          String locator = deviceParams.getLocators()[controllerIndex];
          comms[controllerIndex] = new HydraComm(locator);
        }
        catch (IOException ex)
        {
          comms[controllerIndex] = null;
          logger.log(Level.SEVERE, null, ex);
          return null;
        }
      }
      
      // Get and configure Axis
      try
      {
        HydraAxis axis = new HydraAxis(nodeName + "/detector_t", comms[controllerIndex], axisIndex);
        axis.setAxisUnits("deg");
        axis.setScale(1.0);

        axis.setHasHome(false);
        axis.setHasLimits(true);
        axis.setHasAuxEncoder(false);
        axis.setHasIndex(true);

        axis.setLowerLimitHardRaw(-108.0);
        axis.setUpperLimitHardRaw(+172.5);
        axis.setLowerLimitSoft(axis.getLowerLimitHard() + 0.05);
        axis.setUpperLimitSoft(axis.getUpperLimitHard() - 0.05);
        axis.setInitializeSpeed(1.0);
        axis.setDefaultSpeed(1.0);
        axis.setDefaultAcceleration(10.0);

        axis.enable();
        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        axes[DETECTOR_T] = axis;
        logger.info("... DetectorT Stage connected");
      }
      catch (IOException ex)
      {
        axes[DETECTOR_T] = null;
        logger.log(Level.SEVERE, null, ex);
        return null;
      }
    }
    return axes[DETECTOR_T];
  }
}
