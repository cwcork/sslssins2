//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusServerException;
import cxro.common.io.modbus.BitVector;
import cxro.common.io.modbus.ModbusInterruptedException;

/**
 * Class implementing a <tt>ReadRWDiscretes</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>read coils (FC 1)</i>. It encapsulates
 * the corresponding request and response messages.
 * <p>
 * Coils are understood as bits that can be manipulated
 * (i.e. set or unset).
 *
 * @author Carl Cork
 */
public final class ReadRWDiscretesTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(4);
  private ByteBuffer fResponseData = null;
  private BitVector fDiscretes;

  /**
   * Constructs a new <tt>ReadRWDiscretesTransaction</tt> instance.
   * @param chan a <tt>ModbusChannel</tt> instance.
   */
  public ReadRWDiscretesTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.READ_COILS);
    reqSetReference(0);
    reqSetCount(0);
  }

  /**
   * Constructs a new <tt>ReadRWDiscretesTransaction</tt>
   * instance with a given reference and count of discretes to be read.
   * <p>
   * @param chan a <tt>ModbusChannel</tt> instance.
   * @param ref the reference number of the discrete to read from.
   * @param count the number of bits to be read.
   */
  public ReadRWDiscretesTransaction(ModbusChannel chan, int ref, int count)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.READ_COILS);
    reqSetReference(ref);
    reqSetCount(count);
  }

  /**
   * Returns the reference of the register to to start
   * reading from with this <tt>ReadRWDiscretesTransaction</tt>.
   * <p>
   * @return the reference of the register
   *        to start reading from as <tt>int</tt>.
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference of the register to start reading
   * from with this <tt>ReadRWDiscretesTransaction</tt>.
   * <p>
   * @param ref the reference of the register
   *        to start reading from.
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the number of bits (i.e. coils) to be
   * read with this <tt>ReadRWDiscretesTransaction</tt>.
   * <p>
   * @return the number of bits to be read.
   */
  public int reqGetCount()
  {
    return fRequestData.getChar(2);
  }

  /**
   * Sets the number of bits (i.e. coils) to be read with
   * this <tt>ReadRWDiscretesTransaction</tt>.
   * <p>
   * @param count the number of bits to be read.
   */
  public void reqSetCount(int count)
  {
    if (count > Modbus.MAX_READ_BITS)
    {
      throw new IllegalArgumentException("Maximum bitcount exceeded.");
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
    // 1. Pack request message
    fRequestMsg.setData(fRequestData);
    fRequestValid = true;

    // 2. Perform Modbus transaction
    super.execute();

    // 3. Unpack response message
    fResponseData = fResponseMsg.getData();
    byte[] data = new byte[fResponseData.get(0) & 0xff];
    fResponseData.position(1);
    fResponseData.get(data);
    fDiscretes = new BitVector(data, reqGetCount());
  }

  /**
   * Returns the <tt>BitVector</tt> that stores
   * the collection of bits that have been read.
   * <p>
   * @return the <tt>BitVector</tt> holding the
   *         bits that have been read.
   */
  public BitVector rspGetValues()
  {
    return fDiscretes;
  }

  /**
   * Convenience method that returns the state
   * of the bit at the given index.
   * <p>
   * @param index the index of the coil for which
   *        the status should be returned.
   *
   * @return true if set, false otherwise.
   *
   * @throws IndexOutOfBoundsException if the
   *         index is out of bounds
   */
  public boolean rspGetValue(int index)
    throws IndexOutOfBoundsException
  {

    return fDiscretes.getBit(index);
  }
}

