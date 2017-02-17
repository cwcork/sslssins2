//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import cxro.common.io.modbus.ModbusServerException;

/**
 * Class implementing a <tt>ReadRegistersRW</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>READ_HOLDING_REGISTERS (FC 3)</i>. It encapsulates
 * the corresponding request and response messages.
 * <p>
 * RegistersRW are understood as 16-bit integers that can be manipulated
 * (i.e. set or unset).
 *
 * @author Carl Cork
 */
public final class ReadRWRegistersTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(4);
  private ByteBuffer fResponseData = null;
  private short[] fRegisters = null;

  /**
   * Constructs a new <tt>ReadRWRegistersTransaction</tt> instance.<p>
   * @param chan a <tt>ModbusChannel</tt> instance.
   */
  public ReadRWRegistersTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.READ_HOLDING_REGISTERS);
    reqSetReference(0);
    reqSetCount(0);
  }

  /**
   * Constructs a new <tt>ReadRWRegistersTransaction</tt>
   * instance with a given reference and count of coils
   * (i.e. bits) to be read.
   * <p>
   * @param chan a <tt>ModbusChannel</tt> instance.
   * @param ref the reference number of the register
   *        to read from.
   * @param count the number of bits to be read.
   */
  public ReadRWRegistersTransaction(ModbusChannel chan, int ref, int count)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.READ_HOLDING_REGISTERS);
    reqSetReference(ref);
    reqSetCount(count);
  }

  /**
   * Returns the reference of the register to to start
   * reading from with this <tt>ReadRWRegistersTransaction</tt>.
   * <p>
   * @return the reference of the first register to be read.
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference of the register to start reading
   * from with this <tt>ReadRWRegistersTransaction</tt>.
   * <p>
   * @param ref the reference of the first register to be read.
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the number of registers to be
   * read with this <tt>ReadRWRegistersTransaction</tt>.
   * <p>
   * @return the number of registers to be read.
   */
  public int reqGetCount()
  {
    return fRequestData.getChar(2);
  }

  /**
   * Sets the number of registers to be read.
   * <p>
   * @param count the number of registers to be read.
   */
  public void reqSetCount(int count)
  {
    if (count > Modbus.MAX_READ_REGISTERS)
    {
      throw new IllegalArgumentException("Maximum wordcount exceeded.");
    }
    else
    {
      fRequestData.putShort(2, (short) count);
    }
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
    //1. Pack request message
    fRequestMsg.setData(fRequestData);
    fRequestValid = true;
    
    //2. Perform Modbus transaction
    super.execute();

    //3. Unpack response message
    fResponseData = fResponseMsg.getData();
    fRegisters = new short[(fResponseData.get() & 0xff) >> 1];
    for (int i = 0; i < fRegisters.length; i++)
    {
      fRegisters[i] = fResponseData.getShort();
    }
  }

  /**
   * Returns the <tt>short[]</tt> that stores
   * the collection of registers that have been read.
   * <p>
   * @return an array, <tt>short[]</tt>,
   * holding the registers that have been read.
   */
  public short[] rspGetValues()
  {
    return fRegisters;
  }
  
  /**
   * Returns the <tt>float[]</tt> that stores
   * the collection of registers that have been read.
   * <p>
   * @return an array, <tt>float[]</tt>,
   * holding the registers that have been read.
   */
  public float[] rspGetFloats()
  {
    float[] outArr = null;
    fResponseData.flip();
    outArr = new float[((fResponseData.get() & 0xff) >> 1)/2];
    for (int i = 0; i < outArr.length; i++)
    {
      outArr[i] = fResponseData.getFloat();
    }
    return outArr;
  }

  /**
   * Returns the value of the indexed register.<p>
   * NOTE: For unsigned values assign to an integer as
   * <tt>anInteger = rspGetValue(index) & 0xffff</tt>
   * <p>
   * @param aIndex the index of the register relative to starting reference.
   *
   * @return signed 16-bit integer
   *
   * @throws IndexOutOfBoundsException if the index is out of bounds
   */
  public short rspGetValue(int aIndex)
  {
    return fRegisters[aIndex];
  }
}

