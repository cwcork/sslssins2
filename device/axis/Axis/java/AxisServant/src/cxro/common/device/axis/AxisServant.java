// @license
package cxro.common.device.axis;

import Ice.Current;
import cxro.common.device.IOError;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public final class AxisServant
  extends _AxisIceDisp
{
    private final cxro.common.device.axis.Axis axis;

    public AxisServant(cxro.common.device.axis.Axis axis)
    {
        this.axis = axis;
    }

    @Override
    public String getName(Current __current)
    {
        return axis.getName();
    }

    @Override
    public boolean isEnabled(Current __current)
      throws IOError
    {
        try
        {
            return axis.isEnabled();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void enable(Current __current)
      throws IOError
    {
        try
        {
            axis.enable();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void disable(Current __current)
      throws IOError
    {
        try
        {
            axis.disable();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public boolean isLocked(Current __current)
    {
        return axis.isLocked();
    }

    @Override
    public void unlock(Current __current)
    {
        axis.unlock();
    }

    @Override
    public boolean isStopped(Current __current)
      throws IOError
    {
        try
        {
            return axis.isStopped();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public boolean isReady(Current __current)
      throws IOError
    {
         try
        {
            return axis.isReady();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public boolean isInitialized(Current __current)
      throws IOError
    {
        try
        {
            return axis.isInitialized();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public int initialize(Current __current)
      throws IOError
    {
        try
        {
            return (axis.initialize().get());
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public String getAxisUnits(Current __current)
    {
        return axis.getAxisUnits();
    }

    @Override
    public void setAxisUnits(String units, Current __current)
    {
        axis.setAxisUnits(units);
    }

    @Override
    public double getScale(Current __current)
    {
        return axis.getScale();
    }

    @Override
    public void setScale(double scale, Current __current)
    {
        axis.setScale(scale);
    }

    @Override
    public double getOffset(Current __current)
    {
        return axis.getOffset();
    }

    @Override
    public void setOffset(double offset, Current __current)
    {
        axis.setOffset(offset);
    }

    @Override
    public double getPosition(Current __current)
      throws IOError
    {
        try
        {
            return axis.getPosition();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public double getPositionRaw(Current __current)
      throws IOError
    {
        try
        {
            return axis.getPositionRaw();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void setPosition(double pos, Current __current)
      throws IOError
    {
        try
        {
            axis.setPosition(pos);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public int moveAbsolute(double dest, Ice.Current __current)
      throws IOError
    {
        try
        {
            return (axis.moveAbsolute(dest).get());
        }
        catch (CancellationException ex)
        {
            throw new IOError("CancellationException", ex);
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public int moveAbsoluteRaw(double dest, Current __current)
      throws IOError
    {
        try
        {
            return (axis.moveAbsoluteRaw(dest).get());
        }
        catch (CancellationException ex)
        {
            throw new IOError("CancellationException", ex);
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public int moveRelative(double dist, Ice.Current __current)
      throws IOError
    {
        try
        {
            return (axis.moveRelative(dist).get());
        }
        catch (CancellationException ex)
        {
            throw new IOError("CancellationException", ex);
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public int moveRelativeRaw(double dist, Current __current)
      throws IOError
    {
        try
        {
            return (axis.moveRelativeRaw(dist).get());
        }
        catch (CancellationException ex)
        {
            throw new IOError("CancellationException", ex);
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public double getTarget(Current __current)
      throws IOError
    {
        try
        {
            return (axis.getTarget());
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public int setTarget(double dest, Current __current)
      throws IOError
    {
        try
        {
            return (axis.setTarget(dest));
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public double getTargetRaw(Current __current)
      throws IOError
    {
        try
        {
            return (axis.getTargetRaw());
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public int setTargetRaw(double rawDest, Current __current)
      throws IOError
    {
        try
        {
            return (axis.setTargetRaw(rawDest));
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public int abortMove(Ice.Current __current)
      throws IOError
    {
        try
        {
            return (axis.abortMove().get());
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public int stopMove(Current __current)
      throws IOError
    {
        try
        {
            return (axis.stopMove().get());
        }
        catch (ExecutionException ex)
        {
            throw new IOError("ExecutionException", ex);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
        catch (InterruptedException ex)
        {
            throw new IOError("InterruptedException", ex);
        }
    }

    @Override
    public boolean[] getSwitches(Current __current)
      throws IOError
    {
        try
        {
            return axis.getSwitches();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public double getAcceleration(Current __current)
      throws IOError
    {
        try
        {
            return axis.getAcceleration();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void setAcceleration(double accel, Current __current)
      throws IOError
    {
        try
        {
            axis.setAcceleration(accel);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public double getSpeed(Current __current)
      throws IOError
    {
        try
        {
            return axis.getSpeed();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void setSpeed(double speed, Current __current)
      throws IOError
    {
        try
        {
            axis.setSpeed(speed);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public double getLowerLimitHard(Current __current)
    {
        return axis.getLowerLimitHard();
    }

    @Override
    public double getLowerLimitSoft(Current __current)
    {
        return axis.getLowerLimitSoft();
    }

    @Override
    public int setLowerLimitSoft(double limit, Current __current)
    {
        return axis.setLowerLimitSoft(limit);
    }

    @Override
    public double getUpperLimitSoft(Current __current)
    {
        return axis.getUpperLimitSoft();
    }

    @Override
    public int setUpperLimitSoft(double limit, Current __current)
    {
        return axis.setUpperLimitSoft(limit);
    }

    @Override
    public double getUpperLimitHard(Current __current)
    {
        return axis.getUpperLimitHard();
    }

    @Override
    public boolean hasAuxEncoder(Current __current)
    {
        return axis.hasAuxEncoder();
    }

    @Override
    public double getAuxEncoderScale(Current __current)
    {
        return axis.getAuxEncoderScale();
    }

    @Override
    public void setAuxEncoderScale(double scale, Current __current)
    {
        axis.setAuxEncoderScale(scale);
    }

    @Override
    public double getAuxEncoderOffset(Current __current)
    {
        return axis.getAuxEncoderOffset();
    }

    @Override
    public void setAuxEncoderOffset(double offset, Current __current)
    {
        axis.setAuxEncoderOffset(offset);
    }

    @Override
    public double getAuxEncoderPosition(Current __current)
      throws IOError
    {
        try
        {
            return axis.getAuxEncoderPosition();
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void setAuxEncoderPosition(double pos, Current __current)
      throws IOError
    {
        try
        {
            axis.setAuxEncoderPosition(pos);
        }
        catch (IOException ex)
        {
            throw new IOError("IOException", ex);
        }
    }

    @Override
    public void loadConfigs(Current __current)
    {
        axis.loadConfigs();
    }

    @Override
    public void saveConfigs(Current __current)
    {
        axis.saveConfigs();
    }
}
