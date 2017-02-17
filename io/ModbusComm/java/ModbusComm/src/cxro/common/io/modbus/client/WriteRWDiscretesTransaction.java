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
 * Class implementing a <tt>WriteRWdiscretes</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>(FC 15) Write Multiple Coils</i>. 
 * It encapsulates the corresponding request and response messages.
 * <p>
 * RWdiscretess are understood as addressable bits that are read/write
 *
 * @author Carl Cork
 */
public final class WriteRWDiscretesTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(252);
  private ByteBuffer fResponseData = null;
  private BitVector fbits = null;

  /**
   * Constructs a new <tt>WriteRWDiscretesTransaction</tt> instance.<p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   */
  public WriteRWDiscretesTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_MULTIPLE_COILS);
    fRequestData.putShort(0, (short) 0);
    fRequestData.putShort(2, (short) 0);
    fRequestData.limit(4);
  }

  /**
   * Constructs a new <tt>WriteRWDiscretesTransaction</tt>
   * instance with a given starting reference and values
   * of RWdiscretes (i.e. bits) to be written.
   * <p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   * @param ref the reference number of the first RWdiscrete bit.
   * @param bits the bits to be written.
   */
  public WriteRWDiscretesTransaction(ModbusChannel chan,
                                     int ref, BitVector bits)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_MULTIPLE_COILS);
    reqSetReference(ref);
    reqSetValues(bits);
  }

  /**
   * Returns the reference of the first RWdiscrete to be written.
   * <p>
   * @return aRef - unsigned 16-bit value
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference of the first RWdiscrete to be written.
   * <p>
   * @param ref unsigned 16-bit value
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the number of RWdiscrete bits to be written.
   * <p>
   * @return the number of bits to be written.
   */
  public int reqGetCount()
  {
    return fRequestData.getChar(2);
  }

  /**
   * Returns the <tt>BitVector</tt> that stores
   * the collection of RWdiscrete bits to be written.
   * <p>
   * @return the <tt>BitVector</tt> holding the bits to be written.
   */
  public BitVector reqGetValues()
  {
    return fbits;
  }

  /**
   * Sets the array of bits to be written, starting with <tt>ref</tt>.
   * <p>
   * @param bits the <tt>BitVector</tt> holding the bits to be written.
   */
  public void reqSetValues(BitVector bits)
  {
    fbits = bits;
    byte[] data = fbits.getBytes();

    fRequestData.clear();
    fRequestData.position(2);
    fRequestData.putShort((short) fbits.size());
    fRequestData.put((byte) (data.length & 0xff));
    fRequestData.put(data);
    fRequestData.flip();
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

    // 4. Validate response?
  }
}
