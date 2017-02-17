//@license
package nus.sins2;

import cxro.common.device.axis.Axis;
import java.util.logging.Logger;
import nus.sins2.device.ftr201.FilterStage;
import nus.sins2.device.reflectometer.ReflectometerStage;

/**
 *
 * @author cwcork
 */
public class Sins2Instruments
{
  private static final Logger logger = Logger.getLogger(Sins2Instruments.class.getName());

  private FilterStage filterStage;
  private ReflectometerStage reflectometerStage;

  // ------------------------------- CONSTRUCTORS -------------------------------------------
  /**
   * Construct a SINS2 Instrumentation service locator using the specified base directory.
   * @param userDir base directory for configuration and library files.
   */
  public Sins2Instruments(String userDir)
  {
    // Set working directory and register shutdown handler.
    System.setProperty("user.dir", userDir);
    Runtime.getRuntime().addShutdownHook(new DestroyHook());
    
    // Get devices with default nodenames
    filterStage = new FilterStage();
    reflectometerStage = new ReflectometerStage();
  }

  /**
   * Default constructor.
   * <p>
   * Uses the current value for user.dir.
   */
  public Sins2Instruments()
  {
    this(System.getProperty("user.dir"));
  }

  @Override
  protected void finalize() 
  throws Throwable
  {
    try
    {
      disconnect();
    }
    finally
    {
      super.finalize();
    }
  }
  // ---------------------------------- PUBLIC METHODS ---------------------------------------
  /**
   * Disconnect from devices.
   * 
   * All device objects are nullified and should not be used.
   */
  public final void disconnect()
  {
    if (reflectometerStage != null)
    {
      reflectometerStage.disconnect();
    }
    if (filterStage != null)
    {
      filterStage.disconnect();
    }
  }
  
  public final Axis getFilterStage()
  {
    return filterStage.getAxis();
  }

  public final Axis getMaskX()
  {
    return reflectometerStage.getMaskX();
  }

  public final Axis getMaskY()
  {
    return reflectometerStage.getMaskY();
  }

  public final Axis getMaskZ()
  {
    return reflectometerStage.getMaskZ();
  }

  public final Axis getDetectorX()
  {
    return reflectometerStage.getDetectorX();
  }

  public final Axis getMaskT()
  {
    return reflectometerStage.getMaskT();
  }

  public final Axis getDetectorT()
  {
    return reflectometerStage.getDetectorT();
  }

  // ------------------------- INNER CLASSES ------------------------------
  private class DestroyHook
  extends Thread
  {
    @Override
    public void run()
    {
      disconnect();
    }
  }
}
