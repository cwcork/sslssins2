/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cxro.common.device.axis;

import Ice.InitializationData;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import java.io.IOException;

/**
 *
 * @author cwcork
 */
public class AxisArrayProxy
  implements AxisArray
{
    private String name;
    private Ice.Communicator ic = null;
    private ObjectPrx base;
    private AxisArrayIcePrx axisArrayPrx;
    private AxisProxy[] axes;

    public AxisArrayProxy(String name)
      throws IOException
    {
        this.name = name;
        
        try
        {
            String[] args =
            {
                "--Ice.Config=" + System.getProperty("user.dir") + "/config.proxy"
            };
            System.out.println(args[0]);
            Properties properties = Ice.Util.createProperties(args);
            InitializationData id = new Ice.InitializationData();
            id.properties = properties;
            ic = Util.initialize(id);
            base = ic.stringToProxy(name);
            axisArrayPrx = AxisArrayIcePrxHelper.checkedCast(base);

            // Get axes
            AxisIcePrx[] axesPrx = axisArrayPrx.getAxes();
            axes = new AxisProxy[axesPrx.length];
            for (int i = 0; i < axesPrx.length; i++)
            {
                String axisName = ic.proxyToString(axesPrx[i]);
                axes[i] = new AxisProxy(axisName, ic, axesPrx[i]);
            }
        }
        catch (Ice.LocalException e)
        {
            System.out.println("ICE LocalException");
            throw new IOException(e);
        }
    }

    /**
     * Sees if the communicator can be reached.
     * 
     */
    public boolean ping()
    {
        try
        {
            base.ice_ping();
            return true;
        }
        catch (RuntimeException ex)
        {
            return false;
        }
    }
    
    /**
     * Make sure that communicator is destroyed
     * @throws Throwable
     */
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
    public String getName()
    {
        return this.name;
    }

    @Override
    public int getSize()
    {
        return axes.length;
    }

    @Override
    public Axis[] getAxes()
    {
        return axes.clone();
    }

    @Override
    public Axis getAxis(int index)
    {
        return axes[index];
    }

    @Override
    public void loadConfigs()
    {
        axisArrayPrx.loadConfigs();
    }

    @Override
    public void saveConfigs()
    {
        axisArrayPrx.saveConfigs();
    }
}
