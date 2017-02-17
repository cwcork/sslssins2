// @license
package cxro.common.device.axis;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Interface for all motion control axes.
 * <p>
 * <b>REVISION NOTES:</b><br>
 * 2012-11-03 : Add support for AuxEncoder.<br>
 * <p>
 * @author wcork, cwcork
 */
public interface Axis
{
  //static fields
  /**
   * AXIS_OK = 0
   * <p>
   * Returned from a Future.get().
   * <br>Indicates that method completed successfully.
   */
  public static final int AXIS_OK = 0;
  /**
   * AXIS_BUSY = 2
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that Axis is busy moving.
   */
  public static final int AXIS_BUSY = 2;
  /**
   * DEST_BELOW_LLIMIT = 3
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that move would take Axis below its lower limit.
   */
  public static final int DEST_BELOW_LLIMIT = 3;
  /**
   * DEST_ABOVE_ULIMIT = 4
   * <p>
   * Returned from a Future.get().
   * <br>Indicates that move would take Axis above it upper limit.
   */
  public static final int DEST_ABOVE_ULIMIT = 4;
  /**
   * STOPPED = 5
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that axis was stopped abnormally during its move.
   */
  public static final int STOPPED = 5;
  /**
   * LOCKED = 6
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that axis is locked by another thread which is performing a sequence of
   * uninterruptable operations.
   */
  public static final int LOCKED = 6;
  /**
   * DISABLED = 7
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that axis is disabled and unable to move.
   * <p>
   * @see Axis#enable()
   */
  public static final int DISABLED = 7;
  /**
   * UNINITIALIZED = 8
   * <p>
   * Returned from a Future.get(). <br>
   * Indicates that axis is not initialized.
   * <p>
   * @see Axis#initialize()
   */
  public static final int UNINITIALIZED = 8;

  /**
   * Stop motion, maximum deceleration.
   * <p>
   * This operation can take some time. Therefore it is handled by a separate thread in the driver
   * and the status is returned as a Future, just as for normal moves.
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> abortMove()
  throws IOException;

  /**
   * Cleanup system resources before exit. Needed when using system resources that are not normally
   * handled by Java garbage collector.
   * <p>
   * @throws IOException
   */
  void destroy()
  throws IOException;

  /**
   * Disables axis. This will turn axis motor off.
   * <p>
   * NOTE: This will not assure that the motor is first stopped, nor that a brake will be applied.
   * This must be performed separately by the application program before calling this method.
   * <p>
   * @throws IOException
   */
  void disable()
  throws IOException;

  /**
   * Enables axis. This will turn axis motor on.
   * <p>
   * @throws IOException
   */
  void enable()
  throws IOException;

  /**
   * Get axis acceleration (in SCALED units/s^2).
   * <p>
   * @return acceleration in scaled units
   * <p>
   * @throws IOException
   */
  double getAcceleration()
  throws IOException;

  /**
   * Name for SCALED units.
   * <p>
   * Note that this does NOT define the scale factor. It is merely a guide for operator assistance.
   * See getScale and setScale for actual scale factor settings.
   * <p>
   * @return String units name.
   * <p>
   * @see Axis#setAxisUnits(java.lang.String) setAxisUnits
   */
  String getAxisUnits();

  /**
   * Return lower hardware limit (in SCALED units).
   * <p>
   * This is maintained by the device driver and is not directly modifiable via this interface.
   * <p>
   * @return Lower hardware limit, in SCALED units
   */
  double getLowerLimitHard();

  /**
   * Return lower software limit (in SCALED units).
   * <p>
   * NOTE: This is a software limit. The hardware limit is maintained by the driver and is available
   * via the getLowerLimitHard method.
   * <p>
   * @return Axis lower software limit (scaled units)
   */
  double getLowerLimitSoft();

  /**
   * Returns the given device name of the axis.
   * <p>
   * The name is a readonly parameter that is used to signify the path to configuration data that is
   * maintained by the driver. It represents a hierarchical path to the axis data via the associated
   * component and device elements.
   * <p>
   * For instance, the X axis of the zoneplate stage that is associated with the SHARP endstation,
   * would be given the following name:
   * <ul>
   * <li>cxro&#47;sharp&#47;zp_stage&#47;x</li>
   * </ul>
   * <p>
   * @return Full device name
   */
  String getName();

  /**
   * Returns axis offset (in UNSCALED controller units).
   * <p>
   * OFFSET = POS_UNSCALED - (SCALE * POS_SCALED).<br>
   * POS_SCALED = 0 when POS_UNSCALED = OFFSET.<br>
   * <p>
   * @return OFFSET in unscaled units.
   */
  double getOffset();

  /**
   * Get SCALED axis position.
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47SCALE<br>
   * <p>
   * @return SCALED position
   * <p>
   * @throws IOException
   */
  double getPosition()
  throws IOException;

  /**
   * Get UNSCALED axis position.
   * <p>
   * NOTE: This is a convenience method. This is equivalent to:<br>
   * {@code pos_unscaled = (getScale() * getPosition()) + getOffset();}<br>
   * <p>
   * @return UNSCALED position
   * <p>
   * @throws IOException
   */
  double getPositionRaw()
  throws IOException;

  /**
   * Returns the axis SCALE factor.
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * @return Scale factor.
   */
  double getScale();

  /**
   * Get axis speed (in SCALED units/sec).
   * <p>
   * @return speed in SCALED units.
   * <p>
   * @throws IOException
   */
  double getSpeed()
  throws IOException;

  /**
   * This method returns an array of booleans that represent the current status of the hardware
   * switches on the controller.
   * <p>
   * They are in the form: {Reverse_Switch, Forward_Switch, Home_Switch}.<br>
   * When <u>true</u>, the switch is <u>active</u>.
   * <p>
   * Ex: <br>
   * &nbsp;&nbsp;&nbsp;switches[] = xAxis.getRawSwitches();<br>
   * &nbsp;&nbsp;&nbsp;if(switches[1]) &#47;&#47; test Forward_Switch<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&#47;&#47;throw error or act accordingly.
   * <p>
   * @return switch array.
   * <p>
   * @throws IOException
   */
  boolean[] getSwitches()
  throws IOException;

  /**
   * Returns the destination for the current move (in SCALED units).
   * <p>
   * If motion is completed, the target equals current position.
   * <p>
   * @return target - destination for current move
   * <p>
   * @throws IOException
   */
  double getTarget()
  throws IOException;

  /**
   * Returns the destination for the current move (in UNSCALED units).
   * <p>
   * If motion is completed, the target equals current position.
   * <p>
   * @return target - destination for current move
   * <p>
   * @throws IOException
   */
  double getTargetRaw()
  throws IOException;

  /**
   * Returns the upper software limit (in SCALED units).
   * <p>
   * @return Upper software limit (in scaled units)
   */
  double getUpperLimitSoft();

  /**
   * Returns the upper hardware limit (in SCALED units).
   * <p>
   * This is maintained by the device driver and is not directly modifiable via this interface.
   * <p>
   * @return Upper hardware limit (SCALED).
   */
  double getUpperLimitHard();

  /**
   * Determine if axis has auxiliary encoder.
   * <p>
   * Certain axes have multiple or auxiliary encoders. This is particularly true for some
   * stepper-driven systems where the primary position is determined by the step/microstep count,
   * but a secondary encoder is available for more precise position information.
   * <p>
   * @return true if encoder is present.
   */
  boolean hasAuxEncoder();

  /**
   * Returns the auxiliary encoder SCALE factor.
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * @return Scale factor.
   */
  double getAuxEncoderScale();

  /**
   * Sets the auxiliary encoder SCALE factor.
   * <p>
   * Defines the relationship between SCALED and UNSCALED displacements:<br>
   * <p>
   * DIST_UNSCALED = (SCALE * DIST_SCALED)
   * <p>
   * For example, if the Encoder has 250000 counts/cm and the user wants axis units to be in mm,
   * then the scale is set to 25000.
   * <p>
   * If the axis is inverted, set the scale to negative.
   * <p>
   * @param auxEncoderScale
   */
  void setAuxEncoderScale(double auxEncoderScale);

  /**
   * Returns the auxiliary encoder offset (in UNSCALED controller units).
   * <p>
   * OFFSET = POS_UNSCALED - (SCALE * POS_SCALED).<br>
   * POS_SCALED = 0 when POS_UNSCALED = OFFSET.<br>
   * <p>
   * If hasAuxEncoder = false, then return main encoder offset.
   * <p>
   * @return OFFSET in unscaled units.
   */
  double getAuxEncoderOffset();

  /**
   * Sets the auxiliary encoder offset (in UNSCALED units).
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * If hasAuxEncoder = false, then action is same as setOffset().
   * <p>
   * @param auxEncoderOffset in UNSCALED units.
   */
  void setAuxEncoderOffset(double auxEncoderOffset);

  /**
   * Get SCALED auxiliary encoder position.
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47SCALE<br>
   * <p>
   * If hasAuxEncoder = false, then return main position setting (getPosition).
   * <p>
   * @return SCALED position
   * @throws java.io.IOException
   */
  double getAuxEncoderPosition()
  throws IOException;

  /**
   * Define current auxiliary encoder SCALED position.
   * <p>
   * Sets the current auxiliary encoder position to any defined position in the SCALED coordinate
   * system. <br>
   * NOTE: this does not modify the UNSCALED controller position; rather, it modifies only the
   * OFFSET so that:<br>
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * If hasAuxEncoder = false, then has same behaviour as setPosition.
   * <p>
   * @param auxEncoderPosition New value for current position.
   * @throws java.io.IOException
   */
  void setAuxEncoderPosition(double auxEncoderPosition)
  throws IOException;

  /**
   * Initialize axis.
   * <p>
   * This initiates an initialization operation. In most cases, this is a homing operation which
   * finds either the home limit or encoder index and sets the UNSCALED position to zero.
   * <p>
   * This operation typically takes a long time. Hence the method returns a Future object which can
   * be tested for completion (@see java.util.concurrent.Future).
   * <p>
   * The possible return values include:
   * <ul>
   * <li>{@link cxro.common.device.axis.Axis#AXIS_OK AXIS_OK}
   * <li>{@link cxro.common.device.axis.Axis#AXIS_BUSY AXIS_BUSY}
   * <li>{@link cxro.common.device.axis.Axis#DEST_ABOVE_ULIMIT DEST_ABOVE_ULIMIT}
   * <li>{@link cxro.common.device.axis.Axis#DEST_BELOW_LLIMIT DEST_BELOW_ULIMIT}
   * <li>{@link cxro.common.device.axis.Axis#STOPPED STOPPED}
   * <li>{@link cxro.common.device.axis.Axis#LOCKED LOCKED}
   * <li>{@link cxro.common.device.axis.Axis#DISABLED DISABLED}
   * <li>{@link cxro.common.device.axis.Axis#UNINITIALIZED UNINITIALIZED}
   * </ul>
   * A return value of UNINITIALIZED indicates that the operation failed for unknown reasons.
   * <p>
   * @return Future result of operation (Integer).
   * <p>
   * @throws IOException
   */
  Future<Integer> initialize()
  throws IOException;

  /**
   * Test if the axis has been enabled or disabled by the user.
   * <p>
   * When an axis is disabled, it cannot receive any move commands.
   * <p>
   * @return true if Enabled
   * <p>
   * @throws IOException
   */
  boolean isEnabled()
  throws IOException;

  /**
   * A test if the axis has been homed or initialized in some fashion.
   * <p>
   * Most motion commands are prohibited until the axis is initialized.
   * <p>
   * @see Axis#initialize()
   * @return true if initialized
   * <p>
   * @throws IOException
   */
  boolean isInitialized()
  throws IOException;

  /**
   * Test for Axis locked
   * <p>
   * A lock is set to guard combined command sequences.
   * <p>
   * @return true if currently locked
   */
  boolean isLocked();

  /**
   * Check for axis READY.
   * <p>
   * READY = NOT_MOVING and DRIVE_OK
   * <p>
   * @return true if READY
   * <p>
   * @throws IOException
   */
  boolean isReady()
  throws IOException;

  /**
   * Check for axis STOPPED.
   * <p>
   * STOPPED = NOT MOVING.
   * <p>
   * Does not check for DRIVE_OK. Client programs should usually call isReady() instead.
   * <p>
   * @return true if NOT_MOVING
   * <p>
   * @throws IOException
   */
  boolean isStopped()
  throws IOException;

  /**
   * Load the axis configuration parameters from the driver repository.
   */
  void loadConfigs();

  /**
   * Absolute move in the SCALED coordinate system.
   * <p>
   * The corresponding move in the UNSCALED (controller) units:
   * <p>
   * POS_UNSCALED = (SCALE * POS_SCALED) + OFFSET
   * <p>
   * @param dest move destination, SCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> moveAbsolute(double dest)
  throws IOException;

  /**
   * Absolute move in the UNSCALED coordinate system.
   * <p>
   * This is intended to be used only for special purposes (such as fiducial finding). Normal axis
   * operations should use the corresponding SCALED operation
   * {@link Axis#moveAbsolute(double) moveAbsolute}.
   * <p>
   * NOTE: This is a convenience method. It is equivalent to:<br>
   * <p>
   * {@code moveAbsolute((destRaw - getOffset()) / getScale());}<br>
   * <p>
   * @param destRaw move destination, UNSCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> moveAbsoluteRaw(double destRaw)
  throws IOException;

  /**
   * Relative move in SCALED coordinate system.
   * <p>
   * The corresponding move in the UNSCALED (controller) units:
   * <p>
   * DIST_UNSCALED = (SCALE * DIST_SCALED)
   * <p>
   * @param dist move distance, SCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> moveRelative(double dist)
  throws IOException;

  /**
   * Relative move in UNSCALED coordinate system.
   * <p>
   * This is intended to be used only for special purposes (such as fiducial finding). Normal axis
   * operations should use the corresponding SCALED operation
   * {@link Axis#moveRelative(double) moveRelative}.
   * <p>
   * NOTE: This is a convenience method. It is equivalent to:<br>
   * <p>
   * {@code moveRelative(distRaw / getScale());}<br>
   * <p>
   * @param distRaw move distance, UNSCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> moveRelativeRaw(double distRaw)
  throws IOException;

  /**
   * Save the axis configuration parameters to the driver repository.
   */
  void saveConfigs();

  /**
   * Set axis acceleration (in SCALED units/s^2).
   * <p>
   * @param accel in units/s^2
   * <p>
   * @throws IOException
   */
  void setAcceleration(double accel)
  throws IOException;

  /**
   * Name of SCALED units.
   * <p>
   * Used as a guide to user. This does NOT define the scale factor. See
   * {@link Axis#getScale() getScale} and {@link Axis#setScale(double) setScale} for actual scale
   * factor settings.
   * <p>
   * @param axisUnits
   */
  void setAxisUnits(String axisUnits);

  /**
   * Sets the lower software limit (in SCALED units).
   * <p>
   * This returns the same values as for a move operation, but the DEST_ABOVE_ULIMIT and
   * DEST_BELOW_LLIMIT values have a special meaning here:
   * <ul>
   * <li>DEST_ABOVE_ULIMIT : Software limit would exceed hardware upper limit
   * <li>DEST_BELOW_LLIMIT : Software limit would exceed hardware lower limit
   * </ul>
   * The limit cannot be changed if the Axis is otherwise BUSY or LOCKED by another thread.
   * <p>
   * @param lowerLimit - software lower limit, in SCALED units
   * <p>
   * @return status:<br>
   * &nbsp;&nbsp;AXIS_OK = 0 : Move started. <br>
   * &nbsp;&nbsp;AXIS_BUSY = 2 : Axis not ready.<br>
   * &nbsp;&nbsp;DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * &nbsp;&nbsp;DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * &nbsp;&nbsp;STOPPED = 5 : Axis has been "halted". <br>
   * &nbsp;&nbsp;LOCKED = 6 : Axis has been locked by another thread. <br>
   * &nbsp;&nbsp;DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * &nbsp;&nbsp;UNINITIALIZED = 8 : Axis is not initialized. <br>
   */
  int setLowerLimitSoft(double lowerLimit);

  /**
   * Sets the axis offset (in UNSCALED units).
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * Note that this will have an effect on the software limits. The unscaled value for the soft
   * limits will remain fixed, but the scaled value for the software limits will now be:
   * <p>
   * LIMIT_SCALED = (LIMIT_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * @param offset in UNSCALED units.
   */
  void setOffset(double offset);

  /**
   * Define current SCALED position.
   * <p>
   * Sets the current position to any defined position in the SCALED coordinate system. NOTE: this
   * does not modify the UNSCALED controller position; rather, it modifies the OFFSET so that:<br>
   * <p>
   * POS_SCALED = (POS_UNSCALED - OFFSET)&#47;SCALE<br>
   * <p>
   * @param pos New value for current position.
   * <p>
   * @throws IOException
   */
  void setPosition(double pos)
  throws IOException;

  /**
   * Sets the SCALE factor.
   * <p>
   * Defines the relationship between SCALED and UNSCALED displacements:<br>
   * <p>
   * DIST_UNSCALED = (SCALE * DIST_SCALED)
   * <p>
   * For example, if the Encoder has 250000 counts/cm and the user wants axis units to be in mm,
   * then the scale is set to 25000.
   * <p>
   * If the axis is inverted, set the scale to negative.
   * <p>
   * @param inScale
   */
  void setScale(double inScale);

  /**
   * Set axis speed (in SCALED units/s).
   * <p>
   * @param speed in SCALED units/s.
   * <p>
   * @throws IOException
   */
  void setSpeed(double speed)
  throws IOException;

  /**
   * Set axis target destination (absolute, in SCALED units)
   * <p>
   * @param dest target for move destination, SCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  int setTarget(double dest)
  throws IOException;

  /**
   * Set axis target destination (absolute, in UNSCALED units)
   * <p>
   * @param rawDest target for move destination, UNSCALED
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * DEST_BELOW_LLIMIT = 3 : rawDest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : rawDest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  int setTargetRaw(double rawDest)
  throws IOException;

  /**
   * Sets the upper software limit (in SCALED units).
   * <p>
   * This returns the same values as for a move operation, but the DEST_ABOVE_ULIMIT and
   * DEST_BELOW_LLIMIT values have a special meaning here:
   * <ul>
   * <li>DEST_ABOVE_ULIMIT : Software limit would exceed hardware upper limit
   * <li>DEST_BELOW_LLIMIT : Software limit would exceed hardware lower limit
   * </ul>
   * The limit cannot be changed if the Axis is otherwise BUSY or LOCKED by another thread.
   * <p>
   * @param upperLimit - software upper limit, in SCALED units
   * <p>
   * @return status:<br>
   * &nbsp;&nbsp;AXIS_OK = 0 : Move started. <br>
   * &nbsp;&nbsp;AXIS_BUSY = 2 : Axis not ready.<br>
   * &nbsp;&nbsp;DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * &nbsp;&nbsp;DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * &nbsp;&nbsp;STOPPED = 5 : Axis has been "halted". <br>
   * &nbsp;&nbsp;LOCKED = 6 : Axis has been locked by another thread. <br>
   * &nbsp;&nbsp;DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * &nbsp;&nbsp;UNINITIALIZED = 8 : Axis is not initialized. <br>
   */
  int setUpperLimitSoft(double upperLimit);

  /**
   * Stop motion, normal deceleration.
   * <p>
   * This operation can take some time. Therefore it is handled by a separate thread in the driver
   * and the status is returned as a Future, just as for normal moves.
   * <p>
   * @return status - result at end of move<br>
   * AXIS_OK = 0 : Move started. <br>
   * AXIS_BUSY = 2 : Axis not ready.<br>
   * DEST_BELOW_LLIMIT = 3 : Dest below lower limit, move not initiated.<br>
   * DEST_ABOVE_ULIMIT = 4 : Dest above upper limit, move not initiated.<br>
   * STOPPED = 5 : Axis has been "halted". <br>
   * LOCKED = 6 : Axis has been locked by another thread. <br>
   * DISABLED = 7 : Axis disabled, usually means drive power off.<br>
   * UNINITIALIZED = 8 : Axis is not initialized. <br>
   * <p>
   * @throws IOException
   */
  Future<Integer> stopMove()
  throws IOException;

  /**
   * Release Axis lock.
   * <p>
   * @see Axis#isLocked()
   */
  void unlock();
}
