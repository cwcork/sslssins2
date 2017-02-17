/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cxro.common.device.axis;

import Ice.AsyncResult;
import Ice.Communicator;
import Ice.InitializationData;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import cxro.common.device.IOError;
import cxro.ice.ProxyFuture;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author cwcork
 */
public class AxisProxy
  implements Axis
{
    private String name;
    private Communicator ic = null;
    private AxisIcePrx axis;

    public AxisProxy(String name)
      throws IOException
    {
        this.name = name;

        // Get ice proxy
        try
        {
            String[] args =
            {
                "--Ice.Config=" + System.getProperty("user.dir") +"/config.proxy"
            };
            System.out.println(args[0]);
            Properties properties = Ice.Util.createProperties(args);
            InitializationData id = new Ice.InitializationData();
            id.properties = properties;
            ic = Util.initialize(id);
            ObjectPrx base = ic.stringToProxy(name);
            axis = AxisIcePrxHelper.checkedCast(base);
        }
        catch (Ice.LocalException e)
        {
            System.out.println("ICE LocalException");
            throw new IOException(e);
        }
    }

    public AxisProxy(String name, Communicator ic, AxisIcePrx axis)
      throws IOException
    {
        this.name = name;
        this.ic = ic;
        this.axis = axis;
    }

    /**
     * Make sure that communicator is destroyed
     */
    @Override
    public void destroy()
    {
        if (ic != null)
        {
            // Clean up
            //
            try
            {
                ic.destroy();
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public Future<Integer> abortMove()
      throws IOException
    {
        AbortMoveFuture f = new AbortMoveFuture();
        axis.begin_abortMove(f);
        return f;
    }

    @Override
    public void disable()
      throws IOException
    {
        try
        {
            axis.disable();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void enable()
      throws IOException
    {
        try
        {
            axis.enable();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getAcceleration()
      throws IOException
    {
        try
        {
            return axis.getAcceleration();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public String getAxisUnits()
    {
        return axis.getAxisUnits();
    }

    @Override
    public double getLowerLimitHard()
    {
        return axis.getLowerLimitHard();
    }

    @Override
    public double getLowerLimitSoft()
    {
        return axis.getLowerLimitSoft();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public double getOffset()
    {
        return axis.getOffset();
    }

    @Override
    public double getPosition()
      throws IOException
    {
        try
        {
            return axis.getPosition();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getPositionRaw()
      throws IOException
    {
        try
        {
            return axis.getPositionRaw();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getScale()
    {
        return axis.getScale();
    }

    @Override
    public double getSpeed()
      throws IOException
    {
        try
        {
            return axis.getSpeed();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean[] getSwitches()
      throws IOException
    {
        try
        {
            return axis.getSwitches();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getTarget()
      throws IOException
    {
        try
        {
            return axis.getTarget();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getTargetRaw()
      throws IOException
    {
        try
        {
            return axis.getTargetRaw();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public double getUpperLimitSoft()
    {
        return axis.getUpperLimitSoft();
    }

    @Override
    public double getUpperLimitHard()
    {
        return axis.getUpperLimitHard();
    }

    @Override
    public boolean hasAuxEncoder()
    {
        return axis.hasAuxEncoder();
    }

    @Override
    public double getAuxEncoderScale()
    {
        return axis.getAuxEncoderScale();
    }

    @Override
    public void setAuxEncoderScale(double auxEncoderScale)
    {
        axis.setAuxEncoderScale(auxEncoderScale);
    }

    @Override
    public double getAuxEncoderOffset()
    {
        return axis.getAuxEncoderOffset();
    }

    @Override
    public void setAuxEncoderOffset(double auxEncoderOffset)
    {
        axis.setAuxEncoderOffset(auxEncoderOffset);
    }

    @Override
    public double getAuxEncoderPosition()
      throws IOException
    {
        try
        {
            return axis.getAuxEncoderPosition();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void setAuxEncoderPosition(double auxEncoderPosition)
      throws IOException
    {
        try
        {
            axis.setAuxEncoderPosition(auxEncoderPosition);
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public Future<Integer> initialize()
      throws IOException
    {
        InitializeFuture f = new InitializeFuture();
        axis.begin_initialize(f);
        return f;
    }

    @Override
    public boolean isEnabled()
      throws IOException
    {
        try
        {
            return axis.isEnabled();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean isInitialized()
      throws IOException
    {
        try
        {
            return axis.isInitialized();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean isLocked()
    {
        return axis.isLocked();
    }

    @Override
    public boolean isReady()
      throws IOException
    {
        try
        {
            return axis.isReady();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean isStopped()
      throws IOException
    {
        try
        {
            return axis.isStopped();
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void loadConfigs()
    {
        axis.loadConfigs();
    }

    @Override
    public Future<Integer> moveAbsolute(double dest)
      throws IOException
    {
        MoveAbsoluteFuture f = new MoveAbsoluteFuture();
        axis.begin_moveAbsolute(dest, f);
        return f;
    }

    @Override
    public Future<Integer> moveAbsoluteRaw(double destRaw)
      throws IOException
    {
        MoveAbsoluteRawFuture f = new MoveAbsoluteRawFuture();
        axis.begin_moveAbsoluteRaw(destRaw, f);
        return f;
    }

    @Override
    public Future<Integer> moveRelative(double dist)
      throws IOException
    {
        MoveRelativeFuture f = new MoveRelativeFuture();
        axis.begin_moveRelative(dist, f);
        return f;
    }

    @Override
    public Future<Integer> moveRelativeRaw(double distRaw)
      throws IOException
    {
        MoveRelativeRawFuture f = new MoveRelativeRawFuture();
        axis.begin_moveRelativeRaw(distRaw, f);
        return f;
    }

    @Override
    public void saveConfigs()
    {
        axis.saveConfigs();
    }

    @Override
    public void setAcceleration(double accel)
      throws IOException
    {
        try
        {
            axis.setAcceleration(accel);
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void setAxisUnits(String axisUnits)
    {
        axis.setAxisUnits(axisUnits);
    }

    @Override
    public int setLowerLimitSoft(double lowerLimit)
    {
        return axis.setLowerLimitSoft(lowerLimit);
    }

    @Override
    public void setOffset(double offset)
    {
        axis.setOffset(offset);
    }

    @Override
    public void setPosition(double pos)
      throws IOException
    {
        try
        {
            axis.setPosition(pos);
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public void setScale(double inScale)
    {
        axis.setScale(inScale);
    }

    @Override
    public void setSpeed(double speed)
      throws IOException
    {
        try
        {
            axis.setSpeed(speed);
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public int setTarget(double dest)
      throws IOException
    {
        try
        {
            return (axis.setTarget(dest));
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public int setTargetRaw(double rawDest)
      throws IOException
    {
        try
        {
            return (axis.setTargetRaw(rawDest));
        }
        catch (IOError ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public int setUpperLimitSoft(double upperLimit)
    {
        return axis.setUpperLimitSoft(upperLimit);
    }

    @Override
    public Future<Integer> stopMove()
      throws IOException
    {
        StopMoveFuture f = new StopMoveFuture();
        axis.begin_stopMove(f);
        return f;
    }

    @Override
    public void unlock()
    {
        axis.unlock();
    }

//---------------------------- Private -----------------------------------------
    private class AbortMoveFuture
      extends ProxyFuture<Integer>
    {
        public AbortMoveFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_abortMove(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class InitializeFuture
      extends ProxyFuture<Integer>
    {
        public InitializeFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_initialize(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class MoveAbsoluteFuture
      extends ProxyFuture<Integer>
    {
        public MoveAbsoluteFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_moveAbsolute(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class MoveAbsoluteRawFuture
      extends ProxyFuture<Integer>
    {
        public MoveAbsoluteRawFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_moveAbsoluteRaw(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class MoveRelativeFuture
      extends ProxyFuture<Integer>
    {
        public MoveRelativeFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_moveRelative(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class MoveRelativeRawFuture
      extends ProxyFuture<Integer>
    {
        public MoveRelativeRawFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_moveRelativeRaw(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }

    private class StopMoveFuture
      extends ProxyFuture<Integer>
    {
        public StopMoveFuture()
        {
        }

        @Override
        public void completed(AsyncResult ar)
        {
            lock.lock();
            AxisIcePrx e = (AxisIcePrx) ar.getProxy();
            try
            {
                result = (Integer) e.end_stopMove(ar);
                hasException = false;
            }
            catch (Ice.UserException ex)
            {
                this.ee = new ExecutionException("Ice.UserException", ex);
                hasException = true;
            }
            catch (Ice.LocalException ex)
            {
                ee = new ExecutionException("Ice.LocalException", ex);
                hasException = true;
            }
            finally
            {
                done = true;
                completed.signalAll();
                lock.unlock();
            }
        }
    }
}
