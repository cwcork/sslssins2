//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import cxro.common.io.modbus.ModbusServerException;

/**
 * Class implementing a <tt>WriteRWRegister</tt> transaction.
 * The implementation directly correlates with the modbus
 * function <i>(FC 06) Write Single Register</i>. It encapsulates
 * the corresponding request and response messages.
 * <p>
 * RWRegisters are understood as 16-bit integers that are Read/Write.
 *
 * @author Carl Cork
 */
public final class WriteRWRegisterTransaction
  extends ModbusTransaction
{
  //instance attributes
  private ByteBuffer fRequestData = ByteBuffer.allocate(4);
  private ByteBuffer fResponseData = null;

  /**
   * Constructs a new <tt>WriteRWRegisterTransaction</tt> instance.<p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   */
  public WriteRWRegisterTransaction(ModbusChannel chan)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_SINGLE_REGISTER);
    reqSetReference(0);
    reqSetValue((short) 0);
  }

  /**
   * Constructs a new <tt>WriteRWRegisterTransaction</tt>
   * instance with a given register reference and value to be set.
   * <p>
   * @param chan the <tt>ModbusChannel</tt> to be used by this transaction.
   * @param ref the reference number of the destination register.
   * @param value the 16-bit integer value to be set.
   */
  public WriteRWRegisterTransaction(ModbusChannel chan, int ref, short value)
  {
    super(chan);
    fRequestMsg.setFunctionCode(Modbus.WRITE_SINGLE_REGISTER);
    reqSetReference(ref);
    reqSetValue(value);
  }

  /**
   * Returns the reference address of the registerRW to be modified.
   * <p>
   * @return the reference address (unsigned 16-bit).
   */
  public int reqGetReference()
  {
    return fRequestData.getChar(0);
  }

  /**
   * Sets the reference address of the registerRW to be modified.
   * <p>
   * @param ref the reference address (unsigned 16-bit).
   */
  public void reqSetReference(int ref)
  {
    fRequestData.putShort(0, (short) ref);
  }

  /**
   * Returns the requested 16-bit register value to be set on next
   * <tt>execute()</tt>.
   * <p>
   * @return the value, 16-bit signed integer.
   */
  public short reqGetValue()
  {
    return fRequestData.getShort(2);
  }

  /**
   * Sets the requested 16-bit register value to be set on next
   * <tt>execute()</tt>.
   * <p>
   * @param value 16-bit signed integer.
   */
  public void reqSetValue(short value)
  {
    fRequestData.putShort(2, (short) value);
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

    // 4. Validate reponse message?
  }
}
