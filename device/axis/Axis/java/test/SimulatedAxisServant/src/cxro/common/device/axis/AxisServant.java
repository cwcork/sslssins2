// @license
package cxro.common.device.axis;

import Ice.Current;
import cxro.common.device.IOError;

public final class AxisServant
  extends _AxisIceDisp
{
    private boolean enabled = true;
    private boolean initialized = false;
    private boolean stopped = true;
    private String units = "mm";
    private double scale = 1.0;
    private double offset = 0.0;
    private double raw = 0.0;
    private double acceleration = 100.0;
    private double speed = 10.0;
    private double lowerLimitHardRaw = -10.0;
    private double lowerLimitSoftRaw = 0.0;
    private double upperLimitSoftRaw = 500.0;
    private double upperLimitHardRaw = 1000.0;
    private boolean hasAuxEncoder = true;
    private double auxEncoderScale = 0.1;
    private double auxEncoderOffset = 0.0;
    private double auxEncoderRaw = 0.0;

    public AxisServant()
    {
    }

    @Override
    public String getName(Current __current)
    {
        return "cxro.common.device.axis.mockaxis";
    }

    @Override
    public boolean isEnabled(Current __current)
      throws IOError
    {
        return enabled;
    }

    @Override
    public void enable(Current __current)
      throws IOError
    {
        enabled = true;
    }

    @Override
    public void disable(Current __current)
      throws IOError
    {
        enabled = false;
    }

    @Override
    public boolean isStopped(Current __current)
      throws IOError
    {
        return stopped;
    }

    @Override
    public boolean isReady(Current __current)
      throws IOError
    {
        return true;
    }

    @Override
    public boolean isInitialized(Current __current)
      throws IOError
    {
        return initialized;
    }

    @Override
    public int initialize(Current __current)
      throws IOError
    {
        initialized = true;
        stopped = true;
        raw = 0.0;
        auxEncoderRaw = 0.0;
        return Axis.AXIS_OK;
    }

    @Override
    public String getAxisUnits(Current __current)
    {
        return units;
    }

    @Override
    public void setAxisUnits(String units, Current __current)
    {
        this.units = units;
    }

    @Override
    public double getScale(Current __current)
    {
        return scale;
    }

    @Override
    public void setScale(double scale, Current __current)
    {
        if (Math.abs(scale) < 1e-9)
        {
            if (scale < 0)
            {
                this.scale = -1e-9;
            }
            else
            {
                this.scale = 1e-9;
            }
        }
        else
        {
            this.scale = scale;
        }
    }

    @Override
    public double getOffset(Current __current)
    {
        return offset;
    }

    @Override
    public void setOffset(double offset, Current __current)
    {
        this.offset = offset;
    }

    @Override
    public double getPosition(Current __current)
      throws IOError
    {
        return (raw - offset)/scale;
    }

    @Override
    public double getPositionRaw(Current __current)
      throws IOError
    {
        return raw;
    }

    @Override
    public void setPosition(double pos, Current __current)
      throws IOError
    {
        // Change offset, not raw position
        offset = raw - (scale * pos);
    }

    @Override
    public int moveAbsolute(double dest, Ice.Current __current)
      throws IOError
    {
        raw = (scale * dest) + offset;
        return Axis.AXIS_OK;
    }

    @Override
    public int moveAbsoluteRaw(double dest, Current __current)
      throws IOError
    {
        raw = dest;
        return Axis.AXIS_OK;
    }

    @Override
    public int moveRelative(double dist, Ice.Current __current)
      throws IOError
    {
        raw = raw + (scale * dist);
        return Axis.AXIS_OK;
    }

    @Override
    public int moveRelativeRaw(double dist, Current __current)
      throws IOError
    {
        raw = raw + dist;
        return Axis.AXIS_OK;
    }

    @Override
    public int abortMove(Ice.Current __current)
      throws IOError
    {
        // Simulate an IOError here
        throw new IOError("IOError");
    }

    @Override
    public int stopMove(Current __current)
      throws IOError
    {
        stopped = true;
        return Axis.AXIS_OK;
    }

    @Override
    public boolean[] getSwitches(Current __current)
      throws IOError
    {
        boolean[] switches = {false, false, false};

        return switches;
    }

    @Override
    public double getAcceleration(Current __current)
      throws IOError
    {
        return acceleration;
    }

    @Override
    public void setAcceleration(double accel, Current __current)
      throws IOError
    {
        this.acceleration = Math.abs(accel);
    }

    @Override
    public double getSpeed(Current __current)
      throws IOError
    {
        return speed;
    }

    @Override
    public void setSpeed(double speed, Current __current)
      throws IOError
    {
        this.speed = Math.abs(speed);
    }

    @Override
    public double getLowerLimitHard(Current __current)
    {
        return (lowerLimitHardRaw - offset) / scale;
    }

    @Override
    public double getLowerLimitSoft(Current __current)
    {
        return (lowerLimitSoftRaw - offset) / scale;
    }

    @Override
    public int setLowerLimitSoft(double limit, Current __current)
    {
        double limitRaw = (scale * limit) + offset;
        if (limitRaw < this.lowerLimitHardRaw)
        {
            return Axis.DEST_BELOW_LLIMIT;
        }
        else
        {
            this.lowerLimitSoftRaw = limitRaw;
            return Axis.AXIS_OK;
        }
    }

    @Override
    public double getUpperLimitSoft(Current __current)
    {
        return (upperLimitSoftRaw - offset) / scale;
    }

    @Override
    public int setUpperLimitSoft(double limit, Current __current)
    {
        double limitRaw = (scale * limit) + offset;
        if (limitRaw > this.upperLimitHardRaw)
        {
            return Axis.DEST_ABOVE_ULIMIT;
        }
        else
        {
            this.upperLimitSoftRaw = limitRaw;
            return Axis.AXIS_OK;
        }
    }

    @Override
    public double getUpperLimitHard(Current __current)
    {
        return (upperLimitHardRaw - offset) / scale;
    }

    @Override
    public boolean hasAuxEncoder(Current __current)
    {
        return hasAuxEncoder;
    }

    @Override
    public double getAuxEncoderScale(Current __current)
    {
        return auxEncoderScale;
    }

    @Override
    public void setAuxEncoderScale(double scale, Current __current)
    {
        if (Math.abs(scale) < 1e-9)
        {
            if (scale < 0)
            {
                this.auxEncoderScale = -1e-9;
            }
            else
            {
                this.auxEncoderScale = 1e-9;
            }
        }
        else
        {
            this.auxEncoderScale = scale;
        }
    }

    @Override
    public double getAuxEncoderOffset(Current __current)
    {
        return auxEncoderOffset;
    }

    @Override
    public void setAuxEncoderOffset(double offset, Current __current)
    {
        this.auxEncoderOffset = offset;
    }

    @Override
    public double getAuxEncoderPosition(Current __current)
      throws IOError
    {
        return (auxEncoderRaw - auxEncoderOffset) / auxEncoderScale;
    }

    @Override
    public void setAuxEncoderPosition(double pos, Current __current)
      throws IOError
    {
        auxEncoderRaw = (auxEncoderScale * pos) + auxEncoderOffset;
    }

    @Override
    public void loadConfigs(Current __current)
    {
        // ignore for now
    }

    @Override
    public void saveConfigs(Current __current)
    {
        // ignore for now
    }
}
