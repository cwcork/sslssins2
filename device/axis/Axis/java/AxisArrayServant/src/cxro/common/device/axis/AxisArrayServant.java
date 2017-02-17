// @license
package cxro.common.device.axis;

import Ice.ObjectAdapter;

public final class AxisArrayServant
  extends _AxisArrayIceDisp
{
    private final ObjectAdapter adapter;
    private final AxisArray axisArray;
    private final AxisIcePrx[] axisPrx;

    public
    AxisArrayServant(Ice.ObjectAdapter adapter, AxisArray axisArray)
    {
        this.adapter = adapter;
        this.axisArray = axisArray;

        // Create AxisServants
        Ice.Identity identity;
        axisPrx = new AxisIcePrx[axisArray.getSize()];

        for (int i = 0; i < axisPrx.length; i++)
        {
            identity = adapter.getCommunicator().stringToIdentity("axis" + i);
            adapter.add(new AxisServant(axisArray.getAxis(i)), identity);
            axisPrx[i] = AxisIcePrxHelper.uncheckedCast(adapter.createProxy(identity));
        }
    }

    @Override
    public AxisIcePrx[]
    getAxes(Ice.Current __current)
    {
        return axisPrx.clone();
    }

    public AxisIcePrx
    getAxis(int index, Ice.Current __current)
    {
        return axisPrx[index];
    }

    public String
    getName(Ice.Current __current)
    {
        return axisArray.getName();
    }

    public int
    getSize(Ice.Current __current)
    {
        return axisArray.getSize();
    }

    public void
    loadConfigs(Ice.Current __current)
    {
        axisArray.loadConfigs();
    }

    public void
    saveConfigs(Ice.Current __current)
    {
        axisArray.saveConfigs();
    }
}
