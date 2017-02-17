//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import cxro.common.io.modbus.ModbusServerException;

/**
 * Class implementing a <tt>WriteRWRegisters</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>(FC 16) Write Multiple Registers</i>. 
 * It encapsulates the corresponding request and response messages.
 * <p>
 * RWRegisters are understood as 16-bit integers that are read/write
 *
 * @author Carl Cork
 */
public final class WriteRWRegistersTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(252);
  private ByteBuffer fResponseData = null;
  private short[] fdata = null;

  /**
   * Constructs a new <tt>WriteRWRegistersTransaction</tt> instance.<p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   */
  public WriteRWRegistersTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);
    fRequestData.putShort(0, (short) 0);
    fRequestData.putShort(2, (short) 0);
    fRequestData.limit(4);
  }

  /**
   * Constructs a new <tt>WriteRWRegistersTransaction</tt>
   * instance with a given reference and count of discretesRW
   * (i.e. bits) to be written.
   * <p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   * @param ref the reference number of the first RWRegister.
   * @param values the array of register values to be written.
   */
  public WriteRWRegistersTransaction(ModbusChannel chan,
                                     int ref, short[] values)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);
    reqSetReference(ref);
    reqSetValues(values);
  }

  /**
   * Returns the reference of the first RWRegister to be written.
   * <p>
   * @return aRef - register address, unsigned 16-bit value
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference of the first RWRegister to be written.
   * <p>
   * @param ref unsigned 16-bit value
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the number of RWRegister registers to be written.
   * <p>
   * @return the number of registers to be written.
   */
  public int reqGetCount()
  {
    return fRequestData.getChar(2);
  }

  /**
   * Returns the array of 16-bit register data that will be
   * written during the next {@link WriteRWRegistersTransaction.execute()}.
   * <p>
   * @return array holding the register data to be written.
   */
  public short[] reqGetValues()
  {
    return fdata;
  }
  
  /**
   * Returns the array of 32-bit register data that will be
   * written during the next {@link WriteRWRegistersTransaction.execute()}.
   * <p>
   * @return array holding the register data to be written.
   */
  public float[] reqGetFloats()
  {
    float[] outArr = new float[fdata.length/2];
    fRequestData.asFloatBuffer().get(outArr);
    return outArr;
  }

  /**
   * Sets the array of 16-bit register values to be written,
   * starting with <tt>ref</tt>.
   * <p>
   * @param data array holding the register values.
   */
  public void reqSetValues(short[] data)
  {
    int count = data.length;

    if (count > Modbus.MAX_WRITE_REGISTERS)
    {
      throw new IllegalArgumentException("count out of range [1..123].");
    }

    // Save data for future gets
    fdata = data;

    fRequestData.clear(); //resets limit
    fRequestData.putShort(2, (short) count);
    fRequestData.put(4, (byte) ((count << 1) & 0xff));
    fRequestData.position(5);
    for (int i = 0; i < count; i++)
    {
      fRequestData.putShort((short) fdata[i]);
    }
    fRequestData.flip(); //sets new limit
  }
  
  /**
   * Sets the array of 32-bit float values to be written,
   * starting with <tt>ref</tt>.
   * <p>
   * @param data array holding the register values.
   */
  public void reqSetValues(float[] data)
  {
    int count = data.length*2;

    if (count > Modbus.MAX_WRITE_REGISTERS)
    {
      throw new IllegalArgumentException("count out of range [1..123].");
    }

    // Save data for future gets
    //fdata = data;

    fRequestData.clear(); //resets limit
    fRequestData.putShort(2, (short) count);
    fRequestData.put(4, (byte) ((count << 1) & 0xff));
    fRequestData.position(5);
    for (int i = 0; i < count/2; i++)
    {
      fRequestData.putFloat(data[i]);
    }
    fRequestData.flip(); //sets new limit
    // Save data for future gets
    if(fdata == null)
    {
      fdata = new short[count];
    }
    fRequestData.mark();
    fRequestData.position(5);
    fRequestData.asShortBuffer().get(fdata);
    fRequestData.reset();
  }

  /**
   * Executes this <tt>ModbusTransaction</tt>.
   * <p>
   * 1. Pack request message.
   * 2. Call parent to perform Modbus transaction
   * 3. Unpack response message.
   * <p>
   * @throws ModbusException if an I/O error occurs,
   *         or the response is a modbus protocol exception.
   */
  @Override
  public synchronized void execute()
    throws ModbusIOException,
           ModbusServerException,
           ModbusException,
           ModbusInterruptedException
  {
    // 1. Pack request message
    fRequestMsg.setData(fRequestData);
    fRequestValid = true;

    // 2. Perform Modbus transaction
    super.execute();

    // 3. Unpack response message
    fResponseData = fResponseMsg.getData();

    // 4. Validate response message
  }
}
