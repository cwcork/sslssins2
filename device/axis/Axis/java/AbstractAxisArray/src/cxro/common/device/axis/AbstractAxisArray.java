// @license
package cxro.common.device.axis;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Interface for N Dimensional Devices
 * <p/>
 * @author Carl Cork
 */
public abstract class AbstractAxisArray
    implements AxisArray
{
    // Class fields
    private static final Logger LOGGER = Logger.getLogger(AbstractAxisArray.class.getName());
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
    //----------------------- Constructors -------------------------------------

    protected AbstractAxisArray(String nodeName)
    {
        this.nodeName = nodeName;

        //link to node
        prefs = Preferences.userRoot().node(nodeName);
        //load current settings
        loadConfigs();
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

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSize()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Axis[] getAxes()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Axis getAxis(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadConfigs()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveConfigs()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //static fields
    //instance methods

}
