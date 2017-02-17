//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import cxro.common.io.modbus.ModbusServerException;

/**
 * Class implementing a <tt>WriteSingleDiscreteRW</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>(FC 05) Write Single Coil</i>. It encapsulates
 * the corresponding request and response messages.
 * <p>
 * RWDiscretes are understood as discrete bits that are Read/Write.
 *
 * @author Carl Cork
 */
public final class WriteRWDiscreteTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(4);
  private ByteBuffer fResponseData = null;

  /**
   * Constructs a new <tt>WriteRWDiscreteTransaction</tt> instance.<p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   */
  public WriteRWDiscreteTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_SINGLE_COIL);
    reqSetReference(0);
    reqSetValue(false);
  }

  /**
   * Constructs a new <tt>WriteRWDiscreteTransaction</tt>
   * instance with a given reference and discrete boolean value.
   * <p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   * @param ref the reference number of the discrete bit to be set.
   * @param val the boolean value to be set.
   */
  public WriteRWDiscreteTransaction(ModbusChannel chan, int ref, boolean val)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_SINGLE_COIL);
    reqSetReference(ref);
    reqSetValue(val);
  }

  /**
   * Returns the reference address of the RWdiscrete coil to be modified.
   * <p>
   * @return the reference address (16-bit, unsigned).
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference address of the discreteRW coil to be modified.
   * <p>
   * @param ref the reference address (16-bit, unsigned).
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the requested value to be set for the referenced discreteRW coil.
   * <p>
   * @return the value (<tt>true</tt> = ON, <tt>false</tt> = OFF).
   */
  public boolean reqGetValue()
  {
    return fRequestData.getChar(2) == 0xff00;
  }

  /**
   * Sets the requested value to be set for the referenced discreteRW coil.
   * <p>
   * @param value the requested value
   * (<tt>true</tt> = ON, <tt>false</tt> = OFF).
   */
  public void reqSetValue(boolean value)
  {
    fRequestData.putShort(2, (short) (value ? 0xff00 : 0x0000));
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
