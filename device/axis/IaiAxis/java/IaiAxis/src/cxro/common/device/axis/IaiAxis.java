// @license
package cxro.common.device.axis;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.client.ModbusTcpChannel;
import cxro.common.io.modbus.client.ReadRWRegistersTransaction;
import cxro.common.io.modbus.client.WriteRWDiscreteTransaction;
import cxro.common.io.modbus.client.WriteRWRegistersTransaction;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Device handler for IAI Modbus axis controller.
 * <p>
 * The IAI controller is located on an RS-485 2W network and uses the Modbus/RTU protocol.
 * This device handler expects that it communicates with the controller via a Moxa MB3170
 * Modbus/TCP gateway. Each controller is assigned an individual Modbus/TCP connection.
 * <p>
 * The IAI RS-485 network supports up to 16 controllers.
 * The Moxa MB3170 supports up to 32 simultaneous connections.
 * <p>
 * @author cwcork
 */
public final class IaiAxis
extends AbstractAxis
{
  // Static fields
  private static final Logger logger = Logger.getLogger(IaiAxis.class.getName());
  /**
   * RW Discrete addresses
   */
  private static final int SON_ADDRESS = 0x0403;
  private static final int ALRS_ADDRESS = 0x0407;
  private static final int STP_ADDRESS = 0x040A;
  private static final int HOME_ADDRESS = 0x040B;
  private static final int STOP_ADDRESS = 0x042C;
  /**
   * 32-bit registers
   */
  private static final int PNOW_ADDRESS = 0x9000;
  private static final int PCMD_ADDRESS = 0x9900;
  private static final int VCMD_ADDRESS = 0x9904;
  /**
   * 16-bit registers
   */
  private static final int ALMC_ADDRESS = 0x9002;
  private static final int DSS1_ADDRESS = 0x9005;
  private static final int DSSE_ADDRESS = 0x9007;
  private static final int ACMD_ADDRESS = 0x9906;
   /**
   * Status Flags
   */
  private static final short EMG = (short) 0x8000;
  private static final short PWR = (short) 0x2000;
  private static final short SV = (short) 0x1000;
  private static final short ALMH = (short) 0x0400;
  private static final short ALML = (short) 0x0200;
  private static final short HEND = (short) 0x0010;
  private static final short PEND = (short) 0x0008;
  private static final short RDY = (short) 0x1008;  // (SV | PEND)
  private static final short MOVE = (short) 0x0020;
  //
  // Instance fields
  private final ReadRWRegistersTransaction rRegistersTrans;
  private final WriteRWRegistersTransaction wRegistersTrans;
  private final WriteRWDiscreteTransaction wDiscreteTrans;

  private ModbusTcpChannel chan;
  private String locator;
  private int axisno = 0;
  private int targetRaw = 0;

  // --- Constructors ---
  /**
   * Constructor which uses config file and/or defaults for parameter initialization.
   *
   * @param nodeName configuration node name using java.util.prefs.
   * @param locator  Device communication object.
   * @param axisno   The IAI axis number (0..15).
   * @throws IOException
   */
  public IaiAxis(String nodeName, String locator, int axisno)
  throws IOException
  {
    // Initialize parent class. It calls local loadConfigsSpi()
    super(nodeName);

    //load current settings
    // NOTE: super() also calls loadConfigsSpi(),
    //       but this is overwritten by the defaults and needs reloading.
    loadConfigsSpi();

    // Check for valid axis number
    if ((axisno < 0) || (axisno > 15))
    {
      throw new IllegalArgumentException("axisno must be in range 0..15");
    }
    this.axisno = axisno;

    // Setup channel
    this.locator = locator;
    this.chan = new ModbusTcpChannel(InetAddress.getByName(locator));
    this.chan.setPort(Modbus.DEFAULT_PORT);
    this.axisno = axisno;
    this.chan.setUnitID(axisno + 1); // unitID starts at 1, axisno starts at 0
    this.chan.connect();
    
    // Set specific fixed parameters
    this.setAxisUnits("mm");
    this.setHasAuxEncoder(false);
    this.setHasHome(true);
    this.setHasIndex(false);
    this.setHasLimits(false);
    this.setScale(100.0);
    this.setUpperLimitHardRaw(999999.0);
    this.setLowerLimitHardRaw(-30.0);

    // Setup transactions
    rRegistersTrans = new ReadRWRegistersTransaction(this.chan);
    wRegistersTrans = new WriteRWRegistersTransaction(this.chan);
    wDiscreteTrans = new WriteRWDiscreteTransaction(this.chan);
    
    // Make sure configs are saved
    saveConfigsSpi();
  }

  /**
   * This is a utility constructor to aid with generation of
   * a default configuration file. It is not intended for
   * normal use.
   * @param nodeName
   * @param axisno 
   */
  public IaiAxis(String nodeName, int axisno)
  {
    // Initialize parent class. It calls local loadConfigsSpi()
    super(nodeName);

    // NOTE: super() calls loadConfigsSpi(),
    //       but it doesn't use the default values,
    //       so we must reset them here
    this.axisno = axisno;
    this.setAxisUnits("mm");
    this.setHasAuxEncoder(false);
    this.setHasHome(true);
    this.setHasIndex(false);
    this.setHasLimits(false);
    this.setScale(100.0);
    this.setUpperLimitHardRaw(999999.0);
    this.setLowerLimitHardRaw(-30.0);

    // Make sure configs are saved
    saveConfigsSpi();

    // Set unused fields to null
    rRegistersTrans = null;
    wRegistersTrans = null;
    wDiscreteTrans = null;
  }
  
  //----------------------- PUBLIC    METHODS --------------------------------

  public final String getLocator()
  {
    return locator;
  }

  public final int getAxisNumber()
  {
    return axisno;
  }
  
  public final void reset()
  throws IOException, InterruptedException
  {
    // Attempt to reset alarms
    setAlrs(true);
    Thread.sleep(100);
    // Now clear the reset
    setAlrs(false);
  }

  /**
   * Get the device status value.
   * <p>
   * Returns the IAI specific device status (DSS1 register).
   * @return DSS1 device status word (see IAI manual for details).
   * @throws IOException 
   */
  public final int getStatus() 
  throws IOException
  {
    // Return DSS1 extended to int
    short result = getDss1();
    return (((int)result) & 0xffff);
  }

  /**
   * Get alarm code.
   * <p>
   * Returns the IAI specific device alarm code (ALMC register).
   * @return  alarm code (see IAI manual for details).
   * @throws IOException 
   */
  public final int getAlarmCode() 
  throws IOException
  {
    // Return ALMC extended to int
    short result = getAlmc();
    return (((int)result) & 0xffff);
  }

  //---------- inherited/overridden -------------
  
  @Override
  public void destroy()
  throws IOException
  {
    if (chan != null)
    {
      chan.close();
    }
  }

  @Override
  public void disable() throws IOException
  {
    // Turn off servo
    setSon(false);
  }

  @Override
  public void enable()
  throws IOException
  {
    // First try to clear any outstanding errors
    if (getAlarmCode() != 0)
    {
      try
      {
        reset();
      }
      catch (InterruptedException ignore)
      {
        // ignore
      }
    }
    
    setSon(true);
  }

  @Override
  public boolean[] getSwitches()
  throws IOException
  {
    // There are no limit switches
    // Always return false
    boolean[] result = {false, false, false};
    return result;
  }

  @Override
  public boolean isEnabled()
  throws IOException
  {
    // Test for servo on
    short status = getDss1();
    return ((status & SV) != 0);
  }

  @Override
  public boolean isInitialized()
  throws IOException
  {
    // Test for HEND
    short status = getDss1();
    return ((status & HEND) != 0);
  }

  @Override
  public boolean isReady()
  throws IOException
  {
    // Test for RDY = (SV | PEND)
    short status = getDss1();
    return ((status & RDY) == RDY);
  }

  @Override
  public boolean isStopped()
  throws IOException
  {
    // Test for MOVE bit in DSSE register.
    short status = getDsse();
    return ((status & MOVE) == 0);
  }

  @Override
  public void setPositionRaw(double pos)
  throws IOException
  {
    // This is not supported, the raw position cannot be changed.
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void setAuxEncoderPositionRaw(double pos)
  throws IOException
  {
    // Not supported, no Aux encoder and position is absolute.
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void setInitialized(boolean ready)
  throws IOException
  {
    // Ignore
  }
  
  //----------------------- PROTECTED  METHODS -------------------------------
  @Override
  protected void loadConfigsSpi()
  {
    // There currently are no local properties
  }

  @Override
  protected void saveConfigsSpi()
  {
    // There currently are no local properties
  }

  @Override
  protected void abortMoveSpi()
  throws IOException, InterruptedException
  {
    // Same as stopMoveSpi()
    stopMoveSpi();
  }

  @Override
  protected void stopMoveSpi()
  throws IOException, InterruptedException
  {
    // Issue stop command
    setStop();
    
    // Wait till done
    do
    {
      Thread.sleep(100);
    }
    while (!isReady());
  }

  @Override
  protected void findHomeSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // Start HOME operation with rising edge
    setHome(false);
    setHome(true);
    
    // Wait till initialization is complete
    do
    {
      Thread.sleep(100);
    }
    while (!isInitialized() && !stopFlag);
    
    // Turn off HOME command
    setHome(false);
    
    targetRaw = 0;
  }

  @Override
  protected void findIndexSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // No index reference
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  protected void findLowerLimitSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // No lower limit switch
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  protected void findUpperLimitSpi(double rawspeed)
  throws IOException, InterruptedException
  {
    // No upper limit switch
    throw new UnsupportedOperationException("Not supported."); 
  }

  /**
   * Get acceleration in units of 0.01 mm/s^2.
   * @return
   * @throws IOException 
   */
  @Override
  protected double getAccelerationRawSpi()
  throws IOException
  {
    // Get current commanded acceleration value in units of 0.01 mm/s^2.
    // Internal units are 0.01G.
    double result = (getAcmd() * 9806.65);
    return (result);
  }

  /**
   * Get position in units of 0.01 mm.
   * @return
   * @throws IOException 
   */
  @Override
  protected double getPositionRawSpi()
  throws IOException
  {
    return ((double) getPnow());
  }

  /**
   * Get speed in units of 0.01 mm/s.
   * @return
   * @throws IOException 
   */
  @Override
  protected double getSpeedRawSpi()
  throws IOException
  {
    return ((double) getVcmd());
  }

  /**
   * Get target position in units of 0.01 mm.
   * @return
   * @throws IOException 
   */
  @Override
  protected double getTargetRawSpi()
  throws IOException
  {
    return ((double) getPcmd());
  }

  @Override
  protected void moveAbsoluteRawSpi(double dest)
  throws IOException, InterruptedException
  {
    // Test for range error
    if ((dest < -999999.0) || (dest > 999999.0))
    {
      throw new IllegalArgumentException("Out of range [-999999 .. 999999] : " + dest);
    }
    
    // Set target position
    targetRaw = (int) dest;
    setPcmd(targetRaw);
    
    // Wait till done
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
  }

  @Override
  protected void moveRelativeRawSpi(double dist)
  throws IOException, InterruptedException
  {
    // Actuators only support absolute moves
    double dest = getPnow() + dist;
    
    // Test for range error
    if ((dest < -999999.0) || (dest > 999999.0))
    {
      throw new IllegalArgumentException("Move would be out of range [-999999 .. 999999]");
    }
    
    // Set target postion
    targetRaw = (int) dest;
    setPcmd(targetRaw);
    
    // Wait till done
    do
    {
      Thread.sleep(100);
    }
    while (!isReady() && !stopFlag);
  }

  @Override
  protected void setAccelerationRawSpi(double rawAccel)
  throws IOException
  {
    // rawAccel is in units of 0.01 mm/s^2.
    // controller units are 0.01 G = 98.0665 mm/s^2.
    int accel = (int) (rawAccel * 0.000101972);
    
    // Test for range error
    if ((accel < 1) || (accel > 300))
    {
      throw new IllegalArgumentException("Out of range [9806.65 .. 2941995.0] : " + rawAccel);
    }
    
    // Set target acceleration only if already initialized
    if (isInitialized())
    {
      setAcmd(accel);
    }
  }

  @Override
  protected void setSpeedRawSpi(double rawSpeed)
  throws IOException
  {
    // rawSpeed is in units of 0.01 mm/s.
    // controller units are the same.
    int speed = (int) rawSpeed;
    
    // Test for range error
    if ((rawSpeed < 1) || (rawSpeed > 999999))
    {
      throw new IllegalArgumentException("Out of range [1 .. +999999.0] : " + rawSpeed);
    }
    
    // Set target speed only if already initialized
    if (isInitialized())
    {
      setVcmd(speed);
    }
  }

  @Override
  protected void setTargetRawSpi(double rawDest)
  throws IOException
  {
    // Test for range error
    if ((rawDest < -999999.0) || (rawDest > 999999.0))
    {
      throw new IllegalArgumentException("Out of range [-999999 .. 999999] : " + rawDest);
    }
    
    // Set target position
    targetRaw = (int) rawDest;
    setPcmd(targetRaw);
    
    // Do not wait for motion to complete
  }

  @Override
  protected double getAuxEncoderPositionRawSpi()
  throws IOException
  {
    // IAI actuators do not have an AUX encoder
    // Treat this as if it is regular encoder
    return getPositionRawSpi();
  }

  //----------------------- PACKAGE   METHODS --------------------------------
  //----------------------- PRIVATE   METHODS --------------------------------
  /**
   * Get ALMC Status Word.
   * @return Alarm code (see IAI manual)
   * @throws IOException 
   */
  private synchronized short getAlmc()
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(ALMC_ADDRESS);
      rRegistersTrans.reqSetCount(1);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();
    
    return (res[0]);
  }

  /**
   * Get DSS1 Status Word.
   * <p>
   * NOTE: This is unique to an IAI controller.
   * <p>
   * This is a combination of flag bits
   * <ul>
   * <li> D15 : EMGS, EMG input status
   * <li> D14 : SFTY, Safety speed enabled status
   * <li> D13 : PWR,  Controller ready status
   * <li> D12 : SV,   Servo ON status
   * <li> D11 : PSFL, Missed work part in push-motion operation
   * <li> D10 : ALMH, Major failure status
   * <li> D09 : ALML, Minor failure status
   * <li> D08 : ABER, Absolute error status
   * <li> D07 : BKRL, Brake forced-release status
   * <li> D06 : RESERVED
   * <li> D05 : STP,  Pause status
   * <li> D04 : HEND, Home return completion status
   * <li> D03 : PEND, Position complete status
   * <li> D02 : CEND, Load cell calibration complete
   * <li> D01 : CLBS, Load cell calibration status
   * <li> D00 : RESERVED
   * </ul>
   *
   * @return status word
   * @throws IOException
   */
  private synchronized short getDss1()
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(DSS1_ADDRESS);
      rRegistersTrans.reqSetCount(1);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();
    
    return (res[0]);
  }

  /**
   * Get DSSE status word.
   * <p>
   * NOTE: This is unique to an IAI controller.
   * <p>
   * This is a combination of flag bits
   * <ul>
   * <li> D15 : EMGP, Emergency stop status
   * <li> D14 : MPUV, Motor voltage low status
   * <li> D13 : RMDS, Operation mode status
   * <li> D12 : RESERVED
   * <li> D11 : GHMS, Home return status
   * <li> D10 : PUSH, Push operation in progress
   * <li> D09 : PSNS, Excitation detection status
   * <li> D08 : PMSS, PIO/Modbus switching status
   * <li> D07 : RESERVED
   * <li> D06 : RESERVED
   * <li> D05 : MOVE, Moving status
   * <li> D04 : RESERVED
   * <li> D03 : RESERVED
   * <li> D02 : RESERVED
   * <li> D01 : RESERVED
   * <li> D00 : RESERVED
   * </ul>
   *
   * @return
   * @throws IOException 
   */
  private synchronized short getDsse()
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(DSSE_ADDRESS);
      rRegistersTrans.reqSetCount(1);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();
    
    return (res[0]);
  }

  /**
   * Get the PNOW register value.
   * <p>
   * PNOW holds the current actuator position, in units of 0.01 mm.
   * @return
   * @throws IOException 
   */
  private synchronized int getPnow() 
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(PNOW_ADDRESS);
      rRegistersTrans.reqSetCount(2);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();

    return (((res[0] & 0xffff) << 16) + (res[1] & 0xffff));
  }

  /**
   * Get the PCMD register value.
   * <p>
   * PCMD holds the current target position, in units of 0.01 mm.
   * @return
   * @throws IOException 
   */
  private synchronized int getPcmd() 
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(PCMD_ADDRESS);
      rRegistersTrans.reqSetCount(2);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();

    return (((res[0] & 0xffff) << 16) + (res[1] & 0xffff));
  }

  /**
   * Get the VCMD register value.
   * <p>
   * VCMD holds the current target velocity setting, in units of 0.01 mm/s.
   * @return
   * @throws IOException 
   */
  private synchronized int getVcmd() 
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(VCMD_ADDRESS);
      rRegistersTrans.reqSetCount(2);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();

    return (((res[0] & 0xffff) << 16) + (res[1] & 0xffff));
  }

  /**
   * Get the ACMD register value.
   * <p>
   * ACMD holds the current target acceleration setting, in units of 0.01 G.
   * @return
   * @throws IOException 
   */
  private synchronized int getAcmd() 
  throws IOException
  {
    try
    {
      rRegistersTrans.reqSetReference(ACMD_ADDRESS);
      rRegistersTrans.reqSetCount(1);
      rRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
    
    short[] res = rRegistersTrans.rspGetValues();

    return ((int) res[0]);
  }
  
  /**
   * Set SON discrete.
   * <p>
   * Command ON/OFF of the servo control.
   * When true, the servo will turn ON after "Servo ON delay time".
   * When false, the servo will be turned OFF.
   * @param value true = Servo On, false = Servo Off
   * @throws IOException 
   */
  private synchronized void setSon(boolean value)
  throws IOException
  {
    try
    {
      wDiscreteTrans.reqSetReference(SON_ADDRESS);
      wDiscreteTrans.reqSetValue(value);
      wDiscreteTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set ALRS discrete.
   * <p>
   * When the alarm reset edge is toggled (false->true->false), alarms will be reset.
   * @param value true = Reset On, false = Reset Off
   * @throws IOException 
   */
  private synchronized void setAlrs(boolean value)
  throws IOException
  {
    try
    {
      wDiscreteTrans.reqSetReference(ALRS_ADDRESS);
      wDiscreteTrans.reqSetValue(value);
      wDiscreteTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set STP discrete.
   * <p>
   * All motor movement is inhibited while this bit is 1. 
   * If this bit turns 1 while the actuator is moving, 
   * the actuator will decelerate to a stop. When the bit is set 
   * to 0 again thereafter, the actuator will resume the remaining travel.
   * If this bit is turned 1 while the actuator is performing a home return, 
   * the movement command is held until the actuator reverses upon contact. 
   * When the bit turns 0 thereafter, the actuator will complete the remaining 
   * home return operation automatically. However, make sure you perform a home 
   * return again after the actuator reverses upon contact.
   * @param value true = Pause On, false = Pause Off
   * @throws IOException 
   */
  private synchronized void setStp(boolean value)
  throws IOException
  {
    try
    {
      wDiscreteTrans.reqSetReference(STP_ADDRESS);
      wDiscreteTrans.reqSetValue(value);
      wDiscreteTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set HOME discrete.
   * <p>
   * Home return operation will start on rising edge.
   * Should be reset after homing is complete (HEND = 1).
   * @param value true = Start Home operation, false = Reset Home request.
   * @throws IOException 
   */
  private synchronized void setHome(boolean value)
  throws IOException
  {
    try
    {
      wDiscreteTrans.reqSetReference(HOME_ADDRESS);
      wDiscreteTrans.reqSetValue(value);
      wDiscreteTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set STOP discrete.
   * <p>
   * The actuator will start decelerating to a stop on rising edge.
   * The controller automatically resets the value to false when finished.
   * @throws IOException 
   */
  private synchronized void setStop()
  throws IOException
  {
    try
    {
      wDiscreteTrans.reqSetReference(STOP_ADDRESS);
      wDiscreteTrans.reqSetValue(true);
      wDiscreteTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set PCMD Register.
   * <p>
   * This register specifies the target position using absolute coordinates. 
   * The value of this register is set in units of 0.01 mm. 
   * The actuator will start moving when the register is rewritten.
   * @param value
   * @throws IOException 
   */
  private synchronized void setPcmd(int value)
  throws IOException
  {
    try
    {
      short[] data = new short[2];
      data[0] = (short) ((value >> 16) & 0xffff);
      data[1] = (short) (value & 0xffff);
      wRegistersTrans.reqSetReference(PCMD_ADDRESS);
      wRegistersTrans.reqSetValues(data);
      wRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set VCMD Register.
   * <p>
   * This register specifies the moving speed. 
   * The value of this register is set in units of 0.01 mm/s. 
   * The actuator will start moving when the register is rewritten.
   * @param value
   * @throws IOException 
   */
  private synchronized void setVcmd(int value)
  throws IOException
  {
    try
    {
      short[] data = new short[2];
      data[0] = (short) ((value >> 16) & 0xffff);
      data[1] = (short) (value & 0xffff);
      wRegistersTrans.reqSetReference(VCMD_ADDRESS);
      wRegistersTrans.reqSetValues(data);
      wRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
  
  /**
   * Set ACMD Register.
   * <p>
   * This register specifies the acceleration or deceleration.
   * The value of this register is set in units of 0.01 G in a range of 1 to 300. 
   * The actuator will start moving when the register is rewritten.
   * @param value
   * @throws IOException 
   */
  private synchronized void setAcmd(int value)
  throws IOException
  {
    try
    {
      short[] data = new short[1];
      data[0] = (short) (value & 0xffff);
      wRegistersTrans.reqSetReference(ACMD_ADDRESS);
      wRegistersTrans.reqSetValues(data);
      wRegistersTrans.execute();
    }
    catch (ModbusException ex)
    {
      throw new IOException(ex);
    }
  }
}
