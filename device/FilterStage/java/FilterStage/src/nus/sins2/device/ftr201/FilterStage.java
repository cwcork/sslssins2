// &license
package nus.sins2.device.ftr201;

import cxro.common.device.axis.Axis;
import cxro.common.device.axis.IaiAxis;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Stage controller object for NUS SINS2 FTR201 Stage.
 * This is a device controller for an IAI model ERC3-RA4C-I-42P-3-150-SE-P-CN-B-FL.
 * The ERC3 is a single axis rod-type actuator with built-in controller.
 * Communication with the controller is via a Moxa MB3170 Modbus/TCP to Modbus/RTU gateway.
 * @author Carl Cork &lt;cwcork@lbl.gov&gt;
 */
public class FilterStage
{
  // static fields
  private static final Logger logger = Logger.getLogger(FilterStage.class.getName());

  private static final String DEFAULT_NODENAME = "/nus/sins2/device/ftr201/stage";

  // Static methods
  public static String getDefaultNodename()
  {
    return DEFAULT_NODENAME;
  }
  
  //instance fields
  private final String nodeName;
  private final Preferences devicePrefs;
  private final FilterStageDeviceParameters deviceParams;
  
  private IaiAxis axis;

  // --------------------- Constructors ------------------------------------------------------------
  /**
   * Full constructor for NUS SINS2 Filter stage.
   * <p>
   * Use specified parameter configuration file (Preferences XML file).
   * @param nodeName  configuration node name based on java.util.prefs.
   */
  public FilterStage(String nodeName)
  {
    this.nodeName = nodeName;
    this.devicePrefs = Preferences.userRoot().node(nodeName);
    this.deviceParams = new FilterStageDeviceParameters(this.devicePrefs);
    
    // Initialize axis to null
    axis = null;
    
    // Defer connection until client requests axis
  }
  
  /**
   * Zero argument constructor.
   * <p>
   * Use default configuration nodeName and locators from the configuration.
   */
  public FilterStage() 
  {
    this(DEFAULT_NODENAME);
  }

  public synchronized void disconnect()
  {
    // release resources
    if (axis != null)
    {
      try
      {
        axis.destroy();
      }
      catch (IOException ignore)
      {
        // ignore exception
      }
      axis = null;
    }
  }
  
  public synchronized Axis getAxis()
  {
    if (axis == null)
    {
      logger.info("Attempting to connect to Filter Stage ...");
      try
      {
        // Get axis
        String locator = deviceParams.getLocator();
        int axisno = deviceParams.getAxisno();
        double stroke = deviceParams.getStroke();

        axis = new IaiAxis(nodeName + "/" + axisno, locator, axisno);

        // Configure axis
        axis.setOffset(0.0);
        axis.setScale(100.0);

        axis.setUpperLimitHardRaw((100.0 * stroke) + 30.0); // raw units are 0.01 mm
        axis.setUpperLimitSoft(stroke + 0.01); // cooked units are 1.0 mm
        axis.setLowerLimitSoft(-0.01); // cooked units are 1.0 mm
        axis.setLowerLimitHardRaw(-30.0); // raw units are 0.01 mm

        axis.setDefaultSpeed(10.0);
        axis.setDefaultAcceleration(2941.995); // 0.3 G
        axis.setInitializeSpeed(10.0); // This is actually ignored

        axis.setSpeed(axis.getDefaultSpeed());
        axis.setAcceleration(axis.getDefaultAcceleration());
        
        // success
        logger.info("... Filter Stage connected");
      }
      catch (IOException ex)
      {
        axis = null;
        logger.log(Level.SEVERE, null, ex);
      }
    }
    return axis;
  }
}
