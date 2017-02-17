// @license
package cxro.common.device.axis;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Abstract base class for all motion control axes.
 * <p>
 * @author cwcork
 */
public abstract class AbstractAxis
implements Axis
{
  // Class fields
  private static final Logger logger = Logger.getLogger(AbstractAxis.class.getName());
  //
  // Instance fields
  private final String nodeName;
  private final Preferences prefs;
  private final ExecutorService exec = Executors.newCachedThreadPool();
  private final ReentrantLock rLock = new ReentrantLock();
  //
  private String axisUnits = "mm";
  private double scale = 1.0;
  private double slope = 1.0;
  private double offsetRaw = 0.0;
  //
  private double lowerLimitHardRaw = -1e9;
  private double lowerLimitSoftRaw = -1e9;
  private double upperLimitHardRaw = 1e9;
  private double upperLimitSoftRaw = 1e9;
  private double initializeSpeedRaw = 1.0;
  private double defaultSpeedRaw = 1.0;
  private double defaultAccelerationRaw = 10.0;
  private double auxEncoderScale = 1.0;
  private double auxEncoderSlope = 1.0;
  private double auxEncoderOffsetRaw = 0.0;
  private boolean hasLimits = false;
  private boolean hasHome = false;
  private boolean hasIndex = false;
  private boolean hasAuxEncoder = false;
  //
  // For move operations
  private Future<Integer> m_op = null;
  //
  protected boolean stopFlag = false;
  //
  //----------------------- Constructors -------------------------------------

  protected AbstractAxis(String nodeName)
  {
    this.nodeName = nodeName;

    //link to node
    prefs = Preferences.userRoot().node(nodeName);

    //set defaults
    axisUnits = "mm";
    scale = 1.0;
    slope = 1.0;
    offsetRaw = 0.0;
    lowerLimitHardRaw = -1e9;
    lowerLimitSoftRaw = -1e9;
    upperLimitHardRaw = 1e9;
    upperLimitSoftRaw = 1e9;
    initializeSpeedRaw = 1.0;
    defaultSpeedRaw = 1.0;
    defaultAccelerationRaw = 10.0;
    auxEncoderScale = 1.0;
    auxEncoderSlope = 1.0;
    auxEncoderOffsetRaw = 0.0;
    hasLimits = false;
    hasHome = false;
    hasIndex = false;
    hasAuxEncoder = false;

    //load current settings
    loadConfigsLocal();
  }

  @Override
  public void destroy()
  throws IOException
  {
    // Does nothing by default.
    // If necessary, should be overridden by extended classes.
  }

  //----------------------- PUBLIC    METHODS --------------------------------
  //-----------------------   CONFIGURATION   --------------------------------
  public double getInitializeSpeed()
  {
    return (Math.abs(slope * initializeSpeedRaw));
  }

  public void setInitializeSpeed(double initializeSpeed)
  {
    this.initializeSpeedRaw = Math.abs(scale * initializeSpeed);
    try
    {
      prefs.putDouble("initializeSpeedRaw", initializeSpeedRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public double getDefaultSpeed()
  {
    return (Math.abs(slope * defaultSpeedRaw));
  }

  public void setDefaultSpeed(double defaultSpeed)
  {
    this.defaultSpeedRaw = Math.abs(scale * defaultSpeed);
    try
    {
      prefs.putDouble("defaultSpeedRaw", this.defaultSpeedRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public double getDefaultAcceleration()
  {
    return (Math.abs(slope * defaultAccelerationRaw));
  }

  public void setDefaultAcceleration(double defaultAcceleration)
  {
    this.defaultAccelerationRaw = Math.abs(scale * defaultAcceleration);
    try
    {
      prefs.putDouble("defaultAccelerationRaw", this.defaultAccelerationRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final boolean hasLimits()
  {
    return hasLimits;
  }

  public final void setHasLimits(boolean hasLimits)
  {
    this.hasLimits = hasLimits;
    try
    {
      prefs.putBoolean("hasLimits", hasLimits);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public boolean hasHome()
  {
    return hasHome;
  }

  public void setHasHome(boolean hasHome)
  {
    this.hasHome = hasHome;
    try
    {
      prefs.putBoolean("hasHome", hasHome);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public final boolean hasIndex()
  {
    return hasIndex;
  }

  public final void setHasIndex(boolean hasIndex)
  {
    this.hasIndex = hasIndex;
    try
    {
      prefs.putBoolean("hasIndex", hasIndex);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  public final boolean hasAuxEncoder()
  {
    return hasAuxEncoder;
  }

  public final void setHasAuxEncoder(boolean hasAuxEncoder)
  {
    this.hasAuxEncoder = hasAuxEncoder;
    try
    {
      prefs.putBoolean("hasAuxEncoder", hasAuxEncoder);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  public final void loadConfigs()
  {
    // Load common properties
    loadConfigsLocal();

    //Load SPI configs
    loadConfigsSpi();
  }

  @Override
  public final void saveConfigs()
  {
    // Save common properties
    saveConfigsLocal();

    // Save SPI properties
    saveConfigsSpi();
  }

  // -------------------------  AXIS COMMANDS  -------------------------------
  @Override
  public final Future<Integer> abortMove()
  throws IOException
  {
    // Check if abort is already in progress
    if (stopFlag == true)
    {
      return new ImmediateFuture(STOPPED);
    }
    else // Signal stop
    {
      stopFlag = true;
    }

    // Signal stop
    if ((m_op != null) && !m_op.isDone())
    {
      // NOTE: we cannot interrupt the task as it might split a sar.
      m_op.cancel(false);
    }

    // Perform abort
    try
    {
      abortMoveSpi();
      return new ImmediateFuture(AXIS_OK);
    }
    catch (InterruptedException | RejectedExecutionException | NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  abstract public void disable()
  throws IOException;

  @Override
  abstract public void enable()
  throws IOException;

  @Override
  public final double getAcceleration()
  throws IOException
  {
    return (getAccelerationRawSpi() * Math.abs(slope));
  }

  @Override
  public final String getAxisUnits()
  {
    return axisUnits;
  }

  @Override
  public final double getLowerLimitSoft()
  {
    // return scaled value
    if (slope < 0)
    {
      return (slope * (upperLimitSoftRaw - offsetRaw));
    }
    else
    {
      return (slope * (lowerLimitSoftRaw - offsetRaw));
    }
  }

  @Override
  public final double getLowerLimitHard()
  {
    // return scaled value
    if (slope < 0)
    {
      return (slope * (upperLimitHardRaw - offsetRaw));
    }
    else
    {
      return (slope * (lowerLimitHardRaw - offsetRaw));
    }
  }

  @Override
  public final String getName()
  {
    return nodeName;
  }

  @Override
  public final double getOffset()
  {
    return offsetRaw;
  }

  @Override
  public final double getPosition()
  throws IOException
  {
    return (slope * (getPositionRawSpi() - offsetRaw));
  }

  @Override
  public final double getPositionRaw()
  throws IOException
  {
    return getPositionRawSpi();
  }

  @Override
  public final double getScale()
  {
    return scale;
  }

  @Override
  public final double getSpeed()
  throws IOException
  {
    return (getSpeedRawSpi() * Math.abs(slope));
  }

  @Override
  abstract public boolean[] getSwitches()
  throws IOException;

  @Override
  public double getTarget()
  throws IOException
  {
    return (slope * (getTargetRawSpi() - offsetRaw));
  }

  @Override
  public double getTargetRaw()
  throws IOException
  {
    return getTargetRawSpi();
  }

  @Override
  public final double getUpperLimitSoft()
  {
    // return scaled value
    if (slope < 0)
    {
      return (slope * (lowerLimitSoftRaw - offsetRaw));
    }
    else
    {
      return (slope * (upperLimitSoftRaw - offsetRaw));
    }
  }

  @Override
  public final double getUpperLimitHard()
  {
    // return scaled value
    if (slope < 0)
    {
      return (slope * (lowerLimitHardRaw - offsetRaw));
    }
    else
    {
      return (slope * (upperLimitHardRaw - offsetRaw));
    }
  }

  @Override
  public final Future<Integer> initialize()
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      m_op = exec.submit(new InitializeAxis(initializeSpeedRaw, InitializeAxis.INITIALIZE));
      return (m_op);
    }
    catch (RejectedExecutionException | NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  abstract public boolean isEnabled()
  throws IOException;

  @Override
  abstract public boolean isInitialized()
  throws IOException;

  @Override
  abstract public boolean isReady()
  throws IOException;

  @Override
  abstract public boolean isStopped()
  throws IOException;

  @Override
  public final synchronized Future<Integer> moveAbsolute(double dest)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up absolute move in unscaled units, then start move thread.
      m_op = exec.submit(new MoveAxis(((scale * dest) + offsetRaw), MoveAxis.ABSOLUTE));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  public final synchronized Future<Integer> moveAbsoluteRaw(double dest)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up unscaled absolute move, then start move thread
      m_op = exec.submit(new MoveAxis(dest, MoveAxis.ABSOLUTE));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  public final synchronized Future<Integer> moveRelative(double dist)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up relative move in unscaled units, then start move thread
      m_op = exec.submit(new MoveAxis((scale * dist), MoveAxis.RELATIVE));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  public final synchronized Future<Integer> moveRelativeRaw(double dist)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up unscaled relative move and start move thread
      m_op = exec.submit(new MoveAxis(dist, MoveAxis.RELATIVE));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  public final void setAcceleration(double accel)
  throws IOException
  {
    setAccelerationRawSpi(Math.abs(scale * accel));
  }

  @Override
  public final void setAxisUnits(String axisUnits)
  {
    this.axisUnits = axisUnits;
    try
    {
      prefs.put("axisUnits", this.axisUnits);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  public final int setLowerLimitSoft(double lowerLimit)
  {
    // First convert to unscaled
    double limit = (scale * lowerLimit) + offsetRaw;

    // Make sure this doesn't exceed the hard limits
    if (limit < lowerLimitHardRaw)
    {
      return DEST_BELOW_LLIMIT;
    }
    else if (limit > upperLimitHardRaw)
    {
      return DEST_ABOVE_ULIMIT;
    }

    // Else update limit and save to prefs
    if (scale < 0)
    {
      this.upperLimitSoftRaw = limit;
      try
      {
        prefs.putDouble("upperLimitSoftRaw", this.upperLimitSoftRaw);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    else
    {
      this.lowerLimitSoftRaw = limit;
      try
      {
        prefs.putDouble("lowerLimitSoftRaw", this.lowerLimitSoftRaw);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    return AXIS_OK;
  }

  @Override
  public final synchronized void setOffset(double offset)
  {
    this.offsetRaw = offset;

    // Save to prefs
    try
    {
      prefs.putDouble("offsetRaw", this.offsetRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  public final void setPosition(double pos)
  throws IOException
  {
    // Redefine offset for new position
    setOffset(getPositionRawSpi() - (scale * pos));
  }

  @Override
  public final void setScale(double scale)
  {
    if (scale == 0)
    {
      this.scale = 1e-6;
      this.slope = 1e+6;
    }
    else
    {
      this.scale = scale;
      this.slope = 1 / scale;
    }

    // Save to prefs
    try
    {
      prefs.putDouble("scale", this.scale);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  @Override
  public void setSpeed(double speed)
  throws IOException
  {
    setSpeedRawSpi(Math.abs(scale * speed));
  }

  @Override
  public int setTarget(double dest)
  throws IOException
  {
    double rawDest = (scale * dest) + offsetRaw;

    return (setTargetRaw(rawDest));
  }

  @Override
  public int setTargetRaw(double rawDest)
  throws IOException
  {
    if (rLock.tryLock())
    {
      try
      {
        // Clear stop flag
        stopFlag = false;

        // Check for motor ENABLED and INITIALIZED
        if (!isEnabled())
        {
          return DISABLED;
        }
        if (!isInitialized())
        {
          return UNINITIALIZED;
        }

        // Check that move is within limits
        if (rawDest < lowerLimitSoftRaw)
        {
          return DEST_BELOW_LLIMIT;
        }
        if (rawDest > upperLimitSoftRaw)
        {
          return DEST_ABOVE_ULIMIT;
        }

        // OK, initiate move
        setTargetRawSpi(rawDest);

      }
      catch (IOException ex)
      {
        //TODO: caught and logged or thrown and handled by user?
        throw new IOException(ex);
        //LOGGER.log(Level.WARNING, ex.getMessage());
        //return STOPPED;
      }
      finally
      {
        rLock.unlock();
      }

      return AXIS_OK;
    }
    else
    {
      return LOCKED;
    }
  }

  @Override
  public final int setUpperLimitSoft(double upperLimit)
  {
    // First convert to unscaled
    double limit = (scale * upperLimit) + offsetRaw;

    // Make sure this doesn't exceed the hard limits
    if (limit < lowerLimitHardRaw)
    {
      return DEST_BELOW_LLIMIT;
    }
    else if (limit > upperLimitHardRaw)
    {
      return DEST_ABOVE_ULIMIT;
    }

    // Else update limit and save to prefs
    if (scale < 0)
    {
      this.lowerLimitSoftRaw = limit;
      try
      {
        prefs.putDouble("lowerLimitSoftRaw", this.lowerLimitSoftRaw);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    else
    {
      this.upperLimitSoftRaw = limit;
      try
      {
        prefs.putDouble("upperLimitSoftRaw", this.upperLimitSoftRaw);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    return AXIS_OK;
  }

  @Override
  public final Future<Integer> stopMove()
  throws IOException
  {
    // Check if abort is already in progress
    if (stopFlag == true)
    {
      return new ImmediateFuture(STOPPED);
    }
    else // Signal stop
    {
      stopFlag = true;
    }

    // Signal stop
    if ((m_op != null) && !m_op.isDone())
    {
      // NOTE: we cannot interrupt the task as it might split a sar.
      m_op.cancel(false);
    }

    // Perform stop
    try
    {
      stopMoveSpi();
      return new ImmediateFuture(AXIS_OK);
    }
    catch (InterruptedException | RejectedExecutionException | NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  @Override
  public double getAuxEncoderScale()
  {
    if (hasAuxEncoder)
    {
      return auxEncoderScale;
    }
    else
    {
      return getScale();
    }
  }

  @Override
  public void setAuxEncoderScale(double auxEncoderScale)
  {
    //TODO: find a way to prevent changes while in use
    if (hasAuxEncoder)
    {
      if (auxEncoderScale == 0)
      {
        this.auxEncoderScale = 1e-6;
        this.auxEncoderSlope = 1e+6;
      }
      else
      {
        this.auxEncoderScale = auxEncoderScale;
        this.auxEncoderSlope = 1.0 / auxEncoderScale;
      }

      // Save to prefs
      try
      {
        prefs.putDouble("auxEncoderScale", this.auxEncoderScale);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    else
    {
      setScale(auxEncoderScale);
    }
  }

  @Override
  public double getAuxEncoderOffset()
  {
    if (hasAuxEncoder)
    {
      return auxEncoderOffsetRaw;
    }
    else
    {
      return getOffset();
    }
  }

  @Override
  public void setAuxEncoderOffset(double auxEncoderOffset)
  {
    //TODO: find a way to prevent changes while in use
    if (hasAuxEncoder)
    {
      this.auxEncoderOffsetRaw = auxEncoderOffset;

      // Save to prefs
      try
      {
        prefs.putDouble("auxEncoderOffsetRaw", this.auxEncoderOffsetRaw);
      }
      catch (IllegalStateException ex)
      {
        logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
      }
    }
    else
    {
      setOffset(auxEncoderOffset);
    }
  }

  @Override
  public double getAuxEncoderPosition()
  throws IOException
  {
    if (hasAuxEncoder)
    {
      return (auxEncoderSlope * (getAuxEncoderPositionRawSpi() - auxEncoderOffsetRaw));
    }
    else
    {
      return getPosition();
    }
  }

  @Override
  public void setAuxEncoderPosition(double auxEncoderPosition)
  throws IOException
  {
    //TODO: find a way to prevent changes while in use
    // Redefine offset for new position
    if (hasAuxEncoder)
    {
      setAuxEncoderOffset(getAuxEncoderPositionRawSpi() - (auxEncoderScale * auxEncoderPosition));
    }
    else
    {
      setPosition(auxEncoderPosition);
    }
  }

  //--------------------- DEVICE HANDLER METHODS -----------------------------
  public double getLowerLimitHardRaw()
  {
    return lowerLimitHardRaw;
  }

  public void setLowerLimitHardRaw(double lowerLimitHardRaw)
  {
    this.lowerLimitHardRaw = lowerLimitHardRaw;
    try
    {
      prefs.putDouble("lowerLimitHardRaw", this.lowerLimitHardRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  public double getUpperLimitHardRaw()
  {
    return upperLimitHardRaw;
  }

  public void setUpperLimitHardRaw(double upperLimitHardRaw)
  {
    this.upperLimitHardRaw = upperLimitHardRaw;
    try
    {
      prefs.putDouble("upperLimitHardRaw", this.upperLimitHardRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  /**
   * Sets the current position to the defined raw position.
   * <p>
   * @param pos throws IOException
   * <p>
   * @throws java.io.IOException
   */
  public abstract void setPositionRaw(double pos)
  throws IOException;

  /**
   * Sets the current auxEncoder position to the defined raw position.
   * <p>
   * @param pos throws IOException
   * <p>
   * @throws java.io.IOException
   */
  public abstract void setAuxEncoderPositionRaw(double pos)
  throws IOException;

  /**
   * Begins a motion to the upper maximum value of the axis. Note: Does not set the limit but only
   * moves to it.
   * <p>
   * @param speed A defined speed for the desired motion.
   * <p>
   * @return A future object that lets the user block until motion is stopped.
   * <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> findUpperLimit(double speed)
  throws IOException
  {
    try
    {
      if (!hasLimits)
      {
        throw new UnsupportedOperationException("No Limit Switches");
      }
      else if (rLock.isLocked()) // Check for in use
      {
        return new ImmediateFuture(LOCKED);
      }
      else if (scale < 0)
      {
        m_op = exec.submit(new InitializeAxis(Math.abs(scale * speed),
                                              InitializeAxis.FIND_LOWER_LIMIT));
        return (m_op);
      }
      else
      {
        m_op = exec.submit(new InitializeAxis(Math.abs(scale * speed),
                                              InitializeAxis.FIND_UPPER_LIMIT));
        return (m_op);
      }
    }
    catch (RejectedExecutionException | NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Begins a motion to the lower maximum value of the axis. Note: Does not set the limit but only
   * moves to it.
   * <p>
   * @param speed A defined speed for the desired motion.
   * <p>
   * @return A future object that lets the user block until motion is stopped.
   * <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> findLowerLimit(double speed)
  throws IOException
  {
    try
    {
      if (!hasLimits)
      {
        throw new UnsupportedOperationException("No Limit Switches");
      }
      else if (rLock.isLocked()) // Check for in use
      {
        return new ImmediateFuture(LOCKED);
      }
      else if (scale < 0)
      {
        m_op = exec.submit(new InitializeAxis(Math.abs(scale * speed),
                                              InitializeAxis.FIND_UPPER_LIMIT));
        return (m_op);
      }
      else
      {
        m_op = exec.submit(new InitializeAxis(Math.abs(scale * speed),
                                              InitializeAxis.FIND_LOWER_LIMIT));
        return (m_op);
      }
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Begins a motion in search of the home index.
   * <p>
   * @param speed A defined speed for the desired motion.
   * <p>
   * @return
   *         <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> findHome(double speed)
  throws IOException
  {
    try
    {
      if (!hasHome)
      {
        throw new UnsupportedOperationException("No Home Switch");
      }
      else if (rLock.isLocked()) // Check for in use
      {
        return new ImmediateFuture(LOCKED);
      }
      else
      {
        m_op = exec.submit(new InitializeAxis((scale * speed), InitializeAxis.FIND_HOME));
        return (m_op);
      }
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Begins a motion in search of an index in the direction defined by the sign of the speed param.
   * <p>
   * @param speed A defined speed for the desired motion.
   * <p>
   * @return
   *         <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> findIndex(double speed)
  throws IOException
  {
    try
    {
      if (!hasIndex)
      {
        throw new UnsupportedOperationException("No Encoder Index");
      }
      else if (rLock.isLocked()) // Check for in use
      {
        return new ImmediateFuture(LOCKED);
      }
      else
      {

        m_op = exec.submit(new InitializeAxis((scale * speed), InitializeAxis.FIND_INDEX));
        return (m_op);
      }
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Perform UNSCALED absolute move without initialization and limits checks.
   * <p>
   * @param dest - move destination, UNSCALED
   * <p>
   * @return future status
   * <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> moveAbsoluteRawNoCheck(double dest)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up unscaled absolute move, then start move thread
      m_op = exec.submit(new MoveAxis(dest, MoveAxis.ABSOLUTE_NOCHECK));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Perform UNSCALED relative move without initialization and limits checks.
   * <p>
   * @param dist - move distance, UNSCALED
   * <p>
   * @return future status
   * <p>
   * @throws IOException
   */
  public final synchronized Future<Integer> moveRelativeRawNoCheck(double dist)
  throws IOException
  {
    // Check for in use
    if (rLock.isLocked())
    {
      return new ImmediateFuture(LOCKED);
    }

    try
    {
      // Set up unscaled relative move and start move thread
      m_op = exec.submit(new MoveAxis(dist, MoveAxis.RELATIVE_NOCHECK));
      return (m_op);
    }
    catch (RejectedExecutionException ex)
    {
      throw new IOException(ex);
    }
    catch (NullPointerException ex)
    {
      throw new IOException(ex);
    }
  }

  /**
   * Set AxisInitialized state.
   * <p>
   * NOTE: Normally this should only be set by the Initialize command.
   * <p>
   * @param ready
   *              <p>
   * @throws java.io.IOException
   */
  public abstract void setInitialized(boolean ready)
  throws IOException;

  /**
   * Locks the axis from getting any other motion commands until the axis is then unlocked. <BR>This
   * is used in the case where the user wants to complete uninterrupted complex motions without
   * another process/command from interrupting.
   * <p>
   * An example of this would be a homing procedure.
   */
  public final void lock()
  {
    rLock.lock();
  }

  /**
   * Unlocks the axis from a previous lock.
   */
  @Override
  public final void unlock()
  {
    rLock.unlock();
  }

  /**
   * Tries to see if the axis is locked. If it's already locked, it will return false. If unlocked,
   * the axis will become locked and will return true.
   * <p>
   * @return
   */
  public final boolean trylock()
  {
    return rLock.tryLock();
  }

  /**
   * Test for a previously stated lock.
   * <p>
   * @return true for the axis being locked.
   */
  @Override
  public final boolean isLocked()
  {
    return rLock.isLocked();
  }
  //----------------------- PROTECTED METHODS --------------------------------

  protected final Preferences getPrefs()
  {
    return prefs;
  }

  protected abstract void loadConfigsSpi();

  protected abstract void saveConfigsSpi();

  protected abstract void abortMoveSpi()
  throws IOException, InterruptedException;

  protected abstract void stopMoveSpi()
  throws IOException, InterruptedException;

  protected abstract void findHomeSpi(double rawspeed)
  throws IOException, InterruptedException;

  protected abstract void findIndexSpi(double rawspeed)
  throws IOException, InterruptedException;

  protected abstract void findLowerLimitSpi(double rawspeed)
  throws IOException, InterruptedException;

  protected abstract void findUpperLimitSpi(double rawspeed)
  throws IOException, InterruptedException;

  protected abstract double getAccelerationRawSpi()
  throws IOException;

  protected abstract double getPositionRawSpi()
  throws IOException;

  protected abstract double getSpeedRawSpi()
  throws IOException;

  protected abstract double getTargetRawSpi()
  throws IOException;

  protected abstract void moveAbsoluteRawSpi(double d)
  throws IOException, InterruptedException;

  protected abstract void moveRelativeRawSpi(double d)
  throws IOException, InterruptedException;

  protected abstract void setAccelerationRawSpi(double rawAccel)
  throws IOException;

  protected abstract void setSpeedRawSpi(double rawSpeed)
  throws IOException;

  protected abstract void setTargetRawSpi(double rawDest)
  throws IOException;

  protected abstract double getAuxEncoderPositionRawSpi()
  throws IOException;

  //----------------------- PRIVATE   METHODS --------------------------------
  /**
   * Load properties that are shared by all dependent classes.
   */
  private void loadConfigsLocal()
  {
    this.axisUnits = prefs.get("axisUnits", axisUnits);
    this.scale = prefs.getDouble("scale", scale);
    this.offsetRaw = prefs.getDouble("offsetRaw", offsetRaw);
    this.initializeSpeedRaw = prefs.getDouble("initializeSpeedRaw", initializeSpeedRaw);
    this.defaultSpeedRaw = prefs.getDouble("defaultSpeedRaw", defaultSpeedRaw);
    this.defaultAccelerationRaw = prefs.getDouble("defaultAccelerationRaw", defaultAccelerationRaw);
    this.lowerLimitHardRaw = prefs.getDouble("lowerLimitHardRaw", lowerLimitHardRaw);
    this.lowerLimitSoftRaw = prefs.getDouble("lowerLimitSoftRaw", lowerLimitSoftRaw);
    this.upperLimitHardRaw = prefs.getDouble("upperLimitHardRaw", upperLimitHardRaw);
    this.upperLimitSoftRaw = prefs.getDouble("upperLimitSoftRaw", upperLimitSoftRaw);
    this.hasLimits = prefs.getBoolean("hasLimits", hasLimits);
    this.hasHome = prefs.getBoolean("hasHome", hasHome);
    this.hasIndex = prefs.getBoolean("hasIndex", hasIndex);
    this.hasAuxEncoder = prefs.getBoolean("hasAuxEncoder", hasAuxEncoder);
    this.auxEncoderScale = prefs.getDouble("auxEncoderScale", auxEncoderScale);
    this.auxEncoderOffsetRaw = prefs.getDouble("auxEncoderOffsetRaw", auxEncoderOffsetRaw);

    if (scale == 0)
    {
      scale = 1e-6;
      slope = 1e+6;
    }
    else
    {
      slope = 1.0 / scale;
    }

    if (auxEncoderScale == 0)
    {
      auxEncoderScale = 1e-6;
      auxEncoderSlope = 1e+6;
    }
    else
    {
      auxEncoderSlope = 1.0 / auxEncoderScale;
    }

    //Resynchronize
    saveConfigsLocal();
  }

  /**
   * Save properties that are shared by all dependent classes.
   */
  private void saveConfigsLocal()
  {
    // Save common properties
    try
    {
      if (scale == 0)
      {
        scale = 1e-6;
        slope = 1e+6;
      }
      else
      {
        slope = 1.0 / scale;
      }

      if (auxEncoderScale == 0)
      {
        auxEncoderScale = 1e-6;
        auxEncoderSlope = 1e+6;
      }
      else
      {
        auxEncoderSlope = 1.0 / auxEncoderScale;
      }

      prefs.put("axisUnits", axisUnits);
      prefs.putDouble("scale", scale);
      prefs.putDouble("offsetRaw", offsetRaw);
      prefs.putDouble("initializeSpeedRaw", initializeSpeedRaw);
      prefs.putDouble("defaultSpeedRaw", defaultSpeedRaw);
      prefs.putDouble("defaultAccelerationRaw", defaultAccelerationRaw);
      prefs.putDouble("lowerLimitHardRaw", lowerLimitHardRaw);
      prefs.putDouble("lowerLimitSoftRaw", lowerLimitSoftRaw);
      prefs.putDouble("upperLimitHardRaw", upperLimitHardRaw);
      prefs.putDouble("upperLimitSoftRaw", upperLimitSoftRaw);
      prefs.putBoolean("hasLimits", hasLimits);
      prefs.putBoolean("hasHome", hasHome);
      prefs.putBoolean("hasIndex", hasIndex);
      prefs.putBoolean("hasAuxEncoder", hasAuxEncoder);
      prefs.putDouble("auxEncoderScale", auxEncoderScale);
      prefs.putDouble("auxEncoderOffsetRaw", auxEncoderOffsetRaw);
    }
    catch (IllegalStateException ex)
    {
      logger.log(Level.WARNING, "Node: " + prefs.absolutePath() + " does not exist.", ex);
    }
  }

  //----------------------- PRIVATE   CLASSES --------------------------------
  private class InitializeAxis
  implements Callable<Integer>
  {
    // static fields
    static final int INITIALIZE = 1;
    static final int FIND_HOME = 2;
    static final int FIND_INDEX = 3;
    static final int FIND_LOWER_LIMIT = 4;
    static final int FIND_UPPER_LIMIT = 5;
    // instance fields
    double rawspeed;
    int methodID;

    public InitializeAxis(double rawspeed, int methodID)
    {
      this.rawspeed = rawspeed;
      this.methodID = methodID;
      stopFlag = false;
    }

    @Override
    public Integer call()
    throws IOException
    {
      if (rLock.tryLock())
      {
        try
        {
          // Check for motor ENABLED
          if (!isEnabled())
          {
            return DISABLED;
          }
          // Check for axis ready
          if (!isReady())
          {
            return AXIS_BUSY;
          }
          if (stopFlag)
          {
            return STOPPED;
          }

          // Initiate operation
          setSpeedRawSpi(defaultSpeedRaw);
          setAccelerationRawSpi(defaultAccelerationRaw);
          switch (methodID)
          {
            case INITIALIZE:
              setInitialized(false);
              // findHome, then findIndex
              findHomeSpi(initializeSpeedRaw);
              if (stopFlag)
              {
                return STOPPED;
              }

              if (hasIndex)
              {
                findIndexSpi(initializeSpeedRaw);
                if (stopFlag)
                {
                  return STOPPED;
                }
              }

              setInitialized(true);
              break;
            case FIND_HOME:
              findHomeSpi(rawspeed);
              break;
            case FIND_INDEX:
              findIndexSpi(rawspeed);
              break;
            case FIND_LOWER_LIMIT:
              findLowerLimitSpi(rawspeed);
              break;
            case FIND_UPPER_LIMIT:
              findUpperLimitSpi(rawspeed);
              break;
            default:
              throw new IOException("Improper methodID.");
          }
          if (stopFlag)
          {
            return STOPPED;
          }

          // Wait till axis is ready
          // Should not be required.
          // The Spi methods are supposed to block until done.
          // This is just final assurance.
          while (!isReady())
          {
            if (stopFlag)
            {
              return STOPPED;
            }
            Thread.sleep(100);
          }
        }
        catch (InterruptedException ex)
        {
          //On abortMove() or stopMove()
          return STOPPED;
        }
        catch (IOException ex)
        {
          //TODO: caught and logged or thrown and handled by user?
          throw new IOException(ex);
          //LOGGER.log(Level.WARNING, ex.getMessage());
          //return STOPPED;
        }
        catch (Exception ex)
        {
          // Catch unanticipated runtime exception
          logger.log(Level.SEVERE, "Unanticipated exception", ex);
          throw new IOException(ex);
        }
        finally
        {
          setSpeedRawSpi(defaultSpeedRaw);
          setAccelerationRawSpi(defaultAccelerationRaw);
          rLock.unlock();
        }

        return AXIS_OK;
      }
      else
      {
        return LOCKED;
      }
    }
  }

  /**
   * Separate thread to perform background moves
   */
  private class MoveAxis
  implements Callable<Integer>
  {
    // static fields
    static final int ABSOLUTE = 1;
    static final int RELATIVE = 2;
    static final int ABSOLUTE_NOCHECK = 3;
    static final int RELATIVE_NOCHECK = 4;
    // instance fields
    boolean check = true;
    double value;
    int methodID;

    /**
     *
     * @param value    - all values are in UNSCALED units
     * @param methodID
     *                 <p>
     */
    public MoveAxis(double value, int methodID)
    {
      this.value = value;
      this.methodID = methodID;
      // CHECK before move?
      switch (methodID)
      {
        // normal moves are checked
        case ABSOLUTE:
        case RELATIVE:
          check = true;
          break;
        // special case moves can skip some checks
        case ABSOLUTE_NOCHECK:
        case RELATIVE_NOCHECK:
          check = false;
          break;
        default:
          throw new IllegalArgumentException("Improper methodID");
      }
      stopFlag = false;
    }

    @Override
    public Integer call()
    throws IOException
    {
      if (rLock.tryLock())
      {
        try
        {
          // Check for motor ENABLED
          if (!isEnabled())
          {
            return DISABLED;
          }
          // Check for motor OK and not moving
          if (!isReady())
          {
            return AXIS_BUSY;
          }
          if (stopFlag)
          {
            return STOPPED;
          }

          // Normally, we test for INITIALIZED and LIMITS
          if (check)
          {
            // Check that axis is initialized
            if (!isInitialized())
            {
              return UNINITIALIZED;
            }
            double dest = 0.0;
            switch (methodID)
            {
              case ABSOLUTE:
                dest = value;
                break;

              case RELATIVE:
                dest = value + getPositionRawSpi();
                break;
            }
            // Check that move is within limits
            if (dest < lowerLimitSoftRaw)
            {
              return DEST_BELOW_LLIMIT;
            }
            if (dest > upperLimitSoftRaw)
            {
              return DEST_ABOVE_ULIMIT;
            }
          }

          // OK, initiate move
          switch (methodID)
          {
            case ABSOLUTE:
            case ABSOLUTE_NOCHECK:
              // moveAbsolute
              moveAbsoluteRawSpi(value);
              break;
            case RELATIVE:
            case RELATIVE_NOCHECK:
              // moveRelative
              moveRelativeRawSpi(value);
              break;
          }
          if (stopFlag)
          {
            return STOPPED;
          }

          // Wait till axis is ready
          // Should not be required.
          // The Spi methods are supposed to block until done.
          // This is just final assurance.
          while (!isReady())
          {
            if (stopFlag)
            {
              return STOPPED;
            }
            Thread.sleep(100);
          }
        }
        catch (InterruptedException ex)
        {
          //On stopMove()
          return STOPPED;
        }
        catch (IOException ex)
        {
          //TODO: caught and logged or thrown and handled by user?
          throw new IOException(ex);
          //LOGGER.log(Level.WARNING, ex.getMessage());
          //return STOPPED;
        }
        catch (Exception ex)
        {
          // Catch unanticipated runtime exception
          logger.log(Level.SEVERE, "Unanticipated exception", ex);
          throw new IOException(ex);
        }
        finally
        {
          rLock.unlock();
        }

        return AXIS_OK;
      }
      else
      {
        return LOCKED;
      }
    }
  }

  private class ImmediateFuture
  implements Future<Integer>
  {
    private final Integer value;

    public ImmediateFuture(Integer value)
    {
      this.value = value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
      return false;
    }

    @Override
    public boolean isCancelled()
    {
      return false;
    }

    @Override
    public boolean isDone()
    {
      return true;
    }

    @Override
    public Integer get()
    throws InterruptedException, ExecutionException
    {
      return value;
    }

    @Override
    public Integer get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
    {
      return value;
    }
  }
}
