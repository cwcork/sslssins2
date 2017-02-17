// @license
package cxro.common.device.axis;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Device handler for Simulated axis controller.
 *
 * <p/>
 * @author cwcork
 */
public final class SimulatedAxis
  extends AbstractAxis
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(SimulatedAxis.class.getName());
    //
    // Instance fields
    private boolean enabled = true;
    private boolean initialized = false;
    private boolean stopped = true;
    private boolean ready = true;
    private double acceleration = 0.0;
    private double speed = 0.0;
    private double positionRaw = 0.0;
    private double auxEncoderRaw = 0.0;

    // --- Constructors ---
    /**
     * Constructor which uses config file and/or defaults for parameter
     * initialization.
     * <p/>
     * @param nodeName configuration node name using java.util.prefs.
     * @param comm Device communication object.
     * @throws IOException
     */
    public SimulatedAxis(String nodeName)
      throws IOException
    {
        // Initialize parent class. It calls local loadConfigsSpi()
        super(nodeName);

        // Set simulated configuration
        setAxisUnits("mm");
        setScale(100.0);
        setOffset(0.0);
        setHasLimits(true);
        setHasHome(true);
        setHasIndex(true);
        setAcceleration(100.0);
        setSpeed(10.0);
        setLowerLimitHardRaw(-100.0);
        setLowerLimitSoft(0.0);
        setUpperLimitSoft(500.0);
        setUpperLimitHardRaw(100000.0);
        setHasAuxEncoder(true);
        setAuxEncoderScale(10.0);
        setAuxEncoderOffset(0.0);


        // Make sure configs are saved
        saveConfigs();
    }

    /**
     * Do any cleanup before leaving to garbage collector.
     * Needed by Proxy class for clean exit by Ice.
     */
    @Override
    public final void destroy()
    {
        // For compatibility with SimulatedAxisProxy
        // Needed by Ice
    }
    //----------------------- PUBLIC    METHODS --------------------------------
    //---------- inherited/overridden -------------
    @Override
    public final void disable()
      throws IOException
    {
        //Turn motor off.
        enabled = false;
    }

    @Override
    public final void enable()
      throws IOException
    {
        //Turn motor on.
        enabled = true;
    }

    @Override
    public final boolean[] getSwitches()
      throws IOException
    {
        // for now, leave all switches off
        boolean[] switches =
        {
            false, false, false
        };

        return switches;
    }

    @Override
    public final boolean isEnabled()
      throws IOException
    {
        return enabled;
    }

    @Override
    public final boolean isInitialized()
      throws IOException
    {
        return initialized;
    }

    @Override
    public final boolean isReady()
      throws IOException
    {
        return ready;
    }

    @Override
    public final boolean isStopped()
      throws IOException
    {
        return stopped;
    }

    @Override
    public final synchronized void setPositionRaw(double raw)
      throws IOException
    {
        this.positionRaw = raw;
    }

    @Override
    public final void setAuxEncoderPositionRaw(double raw)
      throws IOException
    {
        this.auxEncoderRaw = raw;
    }

    /**
     * Set AxisInitialized state.
     * <p/>
     * NOTE: Normally this should only be set by the Initialize command.
     * <p/>
     * @param ready throws IOException
     * @ throws InterruptedException
     */
    @Override
    public final void setInitialized(boolean ready)
      throws IOException
    {
        this.initialized = true;
    }

    //----------------------- PROTECTED  METHODS -------------------------------
    @Override
    protected final void loadConfigsSpi()
    {
        // empty
    }

    @Override
    protected final void saveConfigsSpi()
    {
        // empty
    }

    @Override
    protected final void abortMoveSpi()
      throws IOException
    {
        // For simulation purposes, we do a normal stop
        // and then send an IOEception
        stopMoveSpi();
        throw new IOException("abortMove : Simulated IOException");
    }

    @Override
    protected final void stopMoveSpi()
      throws IOException
    {
        try
        {
            // Set initial state
            stopped = false;
            ready = false;

            // Simulate a stop
            Thread.sleep(500);
        }
        catch (InterruptedException ex)
        {
            //Ignore
        }
        finally
        {
            // Set final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final synchronized void findHomeSpi(double rawspeed)
      throws IOException, InterruptedException
    {
        setSpeedRawSpi(rawspeed);

        // Simulate move, assume home is at positionRaw = 0.0;
        try
        {
            // Set initial state
            stopped = false;
            ready = false;

            // Simulate a stop
            Thread.sleep(2000);
            positionRaw = 0.0;
        }
        catch (InterruptedException ex)
        {
            //Ignore
        }
        finally
        {
            // Set final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final synchronized void findIndexSpi(double rawspeed)
      throws IOException, InterruptedException
    {
        setSpeedRawSpi(rawspeed);

        // Simulate move, assume index is at positionRaw = 0.0;
        try
        {
            // Set initial state
            stopped = false;
            ready = false;

            // Simulate a stop
            Thread.sleep(2000);
            positionRaw = 0.0;
        }
        catch (InterruptedException ex)
        {
            //Ignore
        }
        finally
        {
            // Set final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final synchronized void findLowerLimitSpi(double rawspeed)
      throws IOException, InterruptedException
    {
        setSpeedRawSpi(rawspeed);

        // Simulate move, assume upper limit is at lowerLimitHardRaw + 5.0;
        try
        {
            // Set initial state
            stopped = false;
            ready = false;

            // Simulate a stop
            Thread.sleep(2000);
            positionRaw = getUpperLimitHardRaw() + 5.0;
        }
        catch (InterruptedException ex)
        {
            //Ignore
        }
        finally
        {
            // Set final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final synchronized void findUpperLimitSpi(double rawspeed)
      throws IOException, InterruptedException
    {
        setSpeedRawSpi(rawspeed);

        // Simulate move, assume upper limit is at upperLimitHardRaw - 5.0;
        try
        {
            // Set initial state
            stopped = false;
            ready = false;

            // Simulate a stop
            Thread.sleep(2000);
            positionRaw = getUpperLimitHardRaw() - 5.0;
        }
        catch (InterruptedException ex)
        {
            //Ignore
        }
        finally
        {
            // Set final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final double getAccelerationRawSpi()
      throws IOException
    {
        return acceleration;
    }

    @Override
    protected final double getPositionRawSpi()
      throws IOException
    {
        return positionRaw;
    }

    @Override
    protected final double getSpeedRawSpi()
      throws IOException
    {
        return speed;
    }

    @Override
    protected final void setAccelerationRawSpi(double rawAccel)
      throws IOException
    {
        this.acceleration = rawAccel;
    }

    @Override
    protected final void setSpeedRawSpi(double rawSpeed)
      throws IOException
    {
        this.speed = rawSpeed;
    }

    @Override
    protected final void moveAbsoluteRawSpi(double dest)
      throws IOException, InterruptedException
    {
        double relScale = getAuxEncoderScale()/getScale();
        double auxDist = relScale * (dest - positionRaw);
        try
        {
            // Initial state
            stopped = false;
            ready = false;

            // Simulate Move
            Thread.sleep(1000);
            positionRaw = dest;
            auxEncoderRaw += auxDist;
        }
        finally
        {
            // Final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final void moveRelativeRawSpi(double dist)
      throws IOException, InterruptedException
    {
        double relScale = getAuxEncoderScale()/getScale();
        double auxDist = relScale * dist;

        try
        {
            // Initial state
            stopped = false;
            ready = false;

            // Simulate Move
            Thread.sleep(1000);
            positionRaw += dist;
            auxEncoderRaw += auxDist;
        }
        finally
        {
            // Final state
            stopped = true;
            ready = true;
        }
    }

    @Override
    protected final double getAuxEncoderPositionRawSpi()
      throws IOException
    {
        return auxEncoderRaw;
    }
    //----------------------- PACKAGE   METHODS --------------------------------
    //----------------------- PRIVATE   METHODS --------------------------------
}
