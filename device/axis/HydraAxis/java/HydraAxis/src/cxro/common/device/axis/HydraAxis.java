// @license
package cxro.common.device.axis;

import cxro.common.io.HydraComm;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Device handler for MICOS Hydra axis controller.
 * A Hydra axis is a stepper-driven stage, possibly closed-loop.
 * <p>
 * @author cwcork
 */
public final class HydraAxis
extends AbstractAxis
{
  // Class fields
  private static final Logger logger = Logger.getLogger(HydraAxis.class.getName());
  //
  // Instance fields
  private HydraComm comm;
  private int axisno = 1;
  private double targetRaw = 0.0;

  // --- Constructors ---
  /**
   * Constructor which uses config file and/or defaults for parameter initialization.
   * @param nodeName configuration node name using java.util.prefs.
   * @param comm     Device communication object.
   * @throws IOException
   */
  public HydraAxis(String nodeName, HydraComm comm, int axisno)
  throws IOException
  {
    // Initialize parent class. It calls local loadConfigsSpi()
    super(nodeName);

    //load current settings
    // NOTE: super() also calls loadConfigsSpi(),
    //       but this is overwritten by the defaults and needs reloading.
    loadConfigsSpi();

    // Set comm port
    this.comm = comm;
    this.axisno = axisno;

    // Make sure configs are saved
    saveConfigsSpi();
  }

  /**
   * This is a utility constructor to aid with generation of
   * a default configuration file. It is not intended for
   * normal use.
   * @param nodeName
   */
  HydraAxis(String nodeName, int axisno)
  {
    // Initialize parent class. It calls local loadConfigsSpi()
    super(nodeName);

    // NOTE: super() calls loadConfigsSpi(),
    //       but it uses the wrong defaults and needs resaving.
    this.axisno = axisno;

    // Make sure configs are saved
    saveConfigsSpi();
  }
  //----------------------- PUBLIC    METHODS --------------------------------

  public final int getAxisNumber()
  {
    return axisno;
  }

  public void setAxisnumber(int axisno)
  {
    this.axisno = axisno;

    Preferences prefs = getPrefs();
    try
    {
      // Save local properties
      prefs.putInt("axisNumber", axisno);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath().toString() + " does not exist.", ex);
    }
  }

  public final HydraComm getComm()
  {
    return comm;
  }

  public void setComm(HydraComm comm)
  {
    this.comm = comm;
  }

  //---------- inherited/overridden -------------
  @Override
  public final void disable()
  throws IOException
  {
    //Turn motor off.
    snd(String.format("%d motoroff \n", axisno));
  }

  @Override
  public final void enable()
  throws IOException
  {
    //Turn motor on.
    //The error queue also needs purging
    snd(String.format("%d init \n", axisno));
    try
    {
      Thread.sleep(500);
    }
    catch (InterruptedException ex)
    {
      // ignore
    }
    String rsp;
    rsp = sar(String.format("%d gme \n", axisno));
    rsp = sar(String.format("%d gme \n", axisno));
    rsp = sar(String.format("%d gme \n", axisno));
  }

  @Override
  public final boolean[] getSwitches()
  throws IOException
  {
    String cmd = String.format("%d getswst \n", axisno);
    String[] rsp = sar(cmd).split("\\s", 2);
    boolean[] switches =
    {
      Boolean.parseBoolean(rsp[0].trim()),
      Boolean.parseBoolean(rsp[1].trim()),
      false   // No home switch
    };
    return switches;
  }

  @Override
  public final boolean isEnabled()
  throws IOException
  {
    //Check for motor on and not em_stopped
    String rsp = sar(String.format("%d nst \n", axisno));
    return ((Integer.parseInt(rsp) & 0x0380) == 0);
  }

  @Override
  public final boolean isInitialized()
  throws IOException
  {
    /*
         * NOTE: This is a specific 'trick' that uses a 'user variable' to hold
         * the initialization status. It is set after a homing operation and
         * cleared whenever the system is power cycled or reset. We test this
         * status before each move.
     */
    String res = sar(String.format("%d getvarint \n", axisno));
    return (Integer.parseInt(res) != 0);
  }

  @Override
  public final boolean isReady()
  throws IOException
  {
    // READY = NOT_MOVING and DRIVE_OK
    String rsp = sar(String.format("%d nst \n", axisno));
    return ((Integer.parseInt(rsp) & 0x05) == 0);
  }

  @Override
  public final boolean isStopped()
  throws IOException
  {
    String rsp = sar(String.format("%d nst \n", axisno));
    return ((Integer.parseInt(rsp) & 0x01) == 0);
  }

  @Override
  public final void setPositionRaw(double raw)
  throws IOException
  {
    /*
         * NOTE: This method should only be called when you are unable to use
         * the 'findNcalLimit()' method. It does not adjust any other parameters
         * and could result in serious motion problems if not set correctly.
     */
    // The Hydra setnpos command defines the origin relative to
    // the current position. So, we need to use -raw as the
    // input parameter
    snd(String.format("%#f %d setnpos \n", (-raw), axisno));
    targetRaw = raw;
  }

  @Override
  public void setAuxEncoderPositionRaw(double pos)
  throws IOException
  {
    // Since no AuxEncoder, just call setPositionRaw.
    setPositionRaw(pos);
  }

  /**
   * Set AxisInitialized state.
   * <p>
   * NOTE: Normally this should only be set by the Initialize command.
   * <p>
   * @param ready throws IOException @ throws InterruptedException
   */
  @Override
  public final void setInitialized(boolean ready)
  throws IOException
  {
    // Create variable on controller
    if (ready)
    {
      comm.snd(String.format("1 %d setvarint \n", axisno));
    }
    else
    {
      comm.snd(String.format("0 %d setvarint \n", axisno));
    }
  }

  //--------------------------- LOCAL  METHODS -------------------------------
  /**
   * Get RAW Axis Status.
   * <p>
   * NOTE: This is unique to a HYDRA controller.
   * <p>
   * This is a combination of flag bits
   * <ul>
   * <li> D15 : SYSTEM_ERROR, reset required
   * <li> D14 : RESERVED
   * <li> D13 : RESERVED
   * <li> D12 : RESERVED
   * <li> D11 : RESERVED
   * <li> D10 : NOT_READY
   * <li> D09 : EMERGENCY_SWITCH_ACTIVE
   * <li> D08 : MOTOR_OFF
   * <li> D07 : EMERGENCY_STOPPED
   * <li> D06 : RESERVED
   * <li> D05 : WITHIN TARGET WINDOW
   * <li> D04 : RESERVED
   * <li> D03 : RESERVED
   * <li> D02 : MACHINE_ERROR
   * <li> D01 : MANUAL_MODE
   * <li> D00 : MOVING
   * </ul>
   * @return status word
   * @throws IOException
   * @throws InterruptedException
   */
  public final int getStatus() //TODO: Need?
  throws IOException, InterruptedException
  {
    return Integer.parseInt(sar(String.format("%d nst \n", axisno)));
  }

  public final double getUpperRawLimit()
  throws IOException, InterruptedException
  {
    String[] rsp = sar(String.format("%d getnlimit \n", axisno)).split("\\s", 2);
    return (Double.parseDouble(rsp[1]));
  }

  public final double getLowerRawLimit()
  throws IOException, InterruptedException
  {
    String[] rsp = sar(String.format("%d getnlimit \n", axisno)).split("\\s", 2);
    return (Double.parseDouble(rsp[0]));
  }

  public final void setRawLimits(double lowerLimit, double upperLimit)
  throws IOException
  {
    snd(String.format("%#f %#f %d setnlimit \n", lowerLimit, upperLimit, axisno));
  }

  public final void enableNcalLimit()
  throws IOException
  {
    snd(String.format("1 0 %d setsw \n", axisno));
  }

  public final void disableNcalLimit() //TODO: Need?
  throws IOException
  {
    snd(String.format("2 0 %d setsw \n", axisno));
  }

  public final void reset()
  throws IOException
  {
    snd("reset \n");
  }

  public final void enableNrmLimit() //TODO: Need?
  throws IOException
  {
    snd(String.format("1 1 %d setsw \n", axisno));
  }

  public final void disableNrmLimit() //TODO: Need?
  throws IOException
  {
    snd(String.format("2 1 %d setsw \n", axisno));
  }

  /**
   * Test for ncal limit switch enabled.
   * <p>
   * You should not generally need to enable/disable limits!
   * <p>
   * @return switch enable status
   * @throws IOException
   * @throws InterruptedException
   */
  public final boolean isNcalLimitEnabled() //TODO: Need?
  throws IOException, InterruptedException
  {
    String[] rsp = sar(String.format("%d getsw \n", axisno)).split("\\s", 2);
    return (Integer.parseInt(rsp[0].trim()) != 2);
  }

  /**
   * Get Controller System Error.
   * <p>
   * <table class="tableizer-table" {border:
   * 1px solid #CCC; font-family: Arial, Helvetica, sans-serif; font-size:
   * 12px;} .tableizer-table td {padding: 4px; margin: 3px; border: 1px solid
   * #ccc;}> <tr class="tableizer-firstrow" {background-color: #104E8B; color:
   * #FFF; font-weight: bold;}> 
   * <th>Error code</th><th>Description</th>
   * <tr><td>1002</td><td>Parameter stack underrun</td></tr>
   * <tr><td>1003</td><td>Parameter out of range</td></tr>
   * <tr><td>1004</td><td>Position range exceeded *</td></tr>
   * <tr><td>1009</td><td>Para stack lacking space (< 10 para. left)</td></tr>
   * <tr><td>1010</td><td>RS-232 input buffer lacking space (< 30 char. left)</td></tr> 
   * <tr><td>1015</td><td>Limit setting inconsistent</td></tr> 
   * <tr><td>1100</td><td>Limits switches states inconsistent / both active</td></tr> 
   * <tr><td>2000</td><td>Unknown command</td></tr>
   * </table>
   * <p>
   * If a move is terminated by either limit switch in spite of
   * valid motion range limits, this will be indicated by code 1004. (This
   * could, for instance, occur subsequent to loss of motor steps in open loop
   * operation). If a manual alteration of the motion range by setnlimit would
   * as a consequence invalidate the current axis coordinate, the controller
   * will discard the setting, leave the limits unchanged and set code 1015.
   * When a move is meant to target a position outside the currently valid
   * motion range, the axis controller will target the respective position
   * limit instead.
   *
   * @return error code (see Hydra Manual for details)
   * @throws IOException
   */
  public final int getSystemError()
  throws IOException
  {
    String rsp = sar(String.format("%d gne \n", axisno));
    return Integer.parseInt(rsp);
  }

  /**
   * Get Motor Driver Error Status.
   * @return Motor error status (See Hydra manual).
   * @throws IOException
   */
  public final int getMotorError()
  throws IOException
  {
    String rsp = sar(String.format("%d gme \n", axisno));
    return Integer.parseInt(rsp);
  }

  //----------------------- PROTECTED  METHODS -------------------------------
  @Override
  protected final void loadConfigsSpi()
  {
    Preferences prefs = getPrefs();

    // load local properties
    this.axisno = prefs.getInt("axisNumber", axisno);

  }

  @Override
  protected final void saveConfigsSpi()
  {
    Preferences prefs = getPrefs();
    try
    {
      // Save local properties
      prefs.putInt("axisNumber", axisno);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  protected final void abortMoveSpi()
  throws IOException
  {
    // send abort command
    snd(String.format("%d nabort \n", axisno));
    // we need to send a status request, otherwise controller will hang
    try
    {
      Thread.sleep(100);
    }
    catch (InterruptedException ignore)
    {
      // ignore
    }
    sar(String.format("%d nst \n", axisno));
  }

  @Override
  protected final void stopMoveSpi()
  throws IOException
  {
    //send abort/stop command
    snd(String.format("%d nstop \n", axisno));
    // we need to send a status request, otherwise controller will hang
    try
    {
      Thread.sleep(100);
    }
    catch (InterruptedException ignore)
    {
      // ignore
    }
    sar(String.format("%d nst \n", axisno));
  }

  @Override
  protected final void findHomeSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    setSpeedRawSpi(rawspeed);
    snd(String.format("%d ncal \n", axisno));
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
    
    // NOTE: ncal moves set raw position to zero at end of move
    targetRaw = 0.0;
    
    // ignore controller soft limits
    setRawLimits(-200000.0, +200000.0);
  }

  @Override
  protected final void findIndexSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // TODO: add open-loop/closed-loop test
    // TODO: pick a better maxmove value
    setSpeedRawSpi(rawspeed);
    snd(String.format("10000.0 %d nrefmove \n", axisno));
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
    if (stopFlag)
    {
      return;
    }

    // update position
    setPositionRaw(0.0);
    targetRaw = 0.0;
    
    // ignore controller soft limits
    setRawLimits(-200000.0, +200000.0);
  }

  @Override
  protected final void findLowerLimitSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // Ignore if already at limit
    if (!getSwitches()[0])
    {
      //Begin move to limit
      //NOTE: side effect is to set raw position to zero at limit.
      setSpeedRawSpi(rawspeed);
      snd(String.format("%d ncal \n", axisno));
    }
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
    
    // ignore controller soft limits
    setRawLimits(-200000.0, +200000.0);
  }

  @Override
  protected final void findUpperLimitSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // Ignore if already at limit
    if (!getSwitches()[1])
    {
      // Begin move to limit
      // NOTE: side effect is to set soft upper_limit
      setSpeedRawSpi(rawspeed);
      snd(String.format("%d nrm \n", axisno));
    }
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
    
    // ignore controller soft limits
    setRawLimits(-200000.0, +200000.0);
  }

  @Override
  protected final double getAccelerationRawSpi()
  throws IOException
  {
    String rsp = sar(String.format("%d gna \n", axisno));
    return (Double.parseDouble(rsp));
  }

  @Override
  protected final double getPositionRawSpi()
  throws IOException
  {
    String rsp = sar(String.format("%d np \n", axisno));
    return Double.parseDouble(rsp);
  }

  @Override
  protected final double getSpeedRawSpi()
  throws IOException
  {
    String rsp = sar(String.format("%d gnv \n", axisno));
    return (Double.parseDouble(rsp));
  }

  @Override
  protected double getTargetRawSpi()
  throws IOException
  {
    if (isInitialized())
    {
      return targetRaw;
    }
    else
    {
      throw new IOException("Not Initialized");
    }
  }

  @Override
  protected final void setAccelerationRawSpi(double rawAccel)
  throws IOException
  {
    snd(String.format("%#f %d sna \n", rawAccel, axisno));
  }

  @Override
  protected final void setSpeedRawSpi(double rawSpeed)
  throws IOException
  {
    snd(String.format("%#f %d snv \n", rawSpeed, axisno));
  }

  @Override
  protected final void setTargetRawSpi(double rawDest)
  throws IOException
  {
    // Set new target
    snd(String.format("%.4f %d nm \n", rawDest, axisno));
    targetRaw = rawDest;
  }

  @Override
  protected final void moveAbsoluteRawSpi(double dest)
  throws IOException, InterruptedException
  {
    // Start move
    snd(String.format("%.4f %d nm \n", dest, axisno));

    // Wait till done
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
  }

  @Override
  protected final void moveRelativeRawSpi(double dist)
  throws IOException, InterruptedException
  {
    // Start move
    snd(String.format("%.4f %d nr \n", dist, axisno));

    // Wait till done
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
  }

  @Override
  protected final double getAuxEncoderPositionRawSpi()
  throws IOException
  {
    // There is no AuxEncoder, so return same as MainEncoder
    return getPositionRawSpi();
  }
  //----------------------- PACKAGE   METHODS --------------------------------
  //----------------------- PRIVATE   METHODS --------------------------------

  /**
   * Send command. The Hydra can fail silently, so we must check for errors
   * after every command sent.
   *
   * @param command
   * @throws IOException
   */
  private synchronized void snd(String command)
  throws IOException
  {
    comm.snd(command);
  }

  /**
   * Send message and receive response synchronously.
   * Uses default receive timeout of 1 second.
   */
  private synchronized String sar(String command)
  throws IOException
  {
    return sar(command, 1, TimeUnit.SECONDS);
  }

  /**
   * Send message and receive response synchronously.
   * Specify timeout.
   */
  private synchronized String sar(String command, int timeout, TimeUnit unit)
  throws IOException
  {
    try
    {
      //send and receive via serial port
      String res = comm.sar(command, timeout, unit).trim();

      //return response
      return res;
    }
    catch (InterruptedException ex)
    {
      throw new IOException(ex);
    }
  }
}
