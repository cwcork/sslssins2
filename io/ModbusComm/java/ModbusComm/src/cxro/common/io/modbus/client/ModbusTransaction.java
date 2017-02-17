//@license@
package cxro.common.io.modbus.client;

import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import cxro.common.io.modbus.ModbusServerException;

/**
 * Abstract base class implementing a ModbusTransaction.
 * <p>
 * A transaction is defined by the sequence of
 * sending a request message and receiving a
 * related response message. 
 * 
 * @author Dieter Wimberger(original), Carl Cork
 */
public abstract class ModbusTransaction
{
  //instance attributes and associations
  private ModbusChannel fChannel;
  //
  //protected members
  protected ModbusMessage fRequestMsg = new ModbusMessage();
  protected ModbusMessage fResponseMsg = null;
  protected boolean fRequestValid = false;
  protected boolean fResponseValid = false;

  /**
   * Constructs a new <tt>ModbusTransaction</tt>
   * instance with a given <tt>ModbusChannel</tt> to
   * be used for transactions.
   * <p/>
   *
   * @param con a <tt>ModbusChannel</tt> instance.
   */
  protected ModbusTransaction(ModbusChannel chan)
  {
    setChannel(chan);
  }//constructor

  /**
   * Gets the connection on which this <tt>ModbusTransaction</tt>
   * should be executed.<p>
   *
   * @return chan - a <tt>ModbusChannel</tt>.
   */
  public final ModbusChannel getChannel()
  {
    return fChannel;
  }

  /**
   * Sets the connection on which this <tt>ModbusTransaction</tt>
   * should be executed.<p>
   *
   * @param chan a <tt>ModbusChannel</tt>.
   */
  public final void setChannel(ModbusChannel chan)
  {
    fChannel = chan;
  }

  /**
   * Executes this <tt>ModbusTransaction</tt>.
   *
   * @throws ModbusException if an I/O error occurs,
   *         or the response is a modbus protocol exception.
   */
  public synchronized void execute()
    throws ModbusException,
           ModbusIOException,
           ModbusServerException,
           ModbusInterruptedException
  {
    // 1. Make certain we are executable
    if (!fRequestValid || fChannel == null)
    {
      throw new ModbusException("Transaction not executable");
    }

    // 2. Excecute transaction
    fResponseValid = false;
    fResponseMsg = fChannel.execute(fRequestMsg);

    // 3. Deal with "application level" exceptions
    if (fResponseMsg.isException())
    {
      throw new ModbusServerException(fResponseMsg.getExceptionCode());
    }

    fResponseValid = true;
  }

  /**
   * Get requestMsg as hexadecimal string.
   *
   * @return toHexString
   */
  public String requestMsgToHexString()
  {
    return fRequestMsg.toHexString();
  }

  /**
   * Get responseMsg as hexadecimal string.
   *
   * @return toHexString
   */
  public String responseMsgToHexString()
  {
    return fResponseMsg.toHexString();
  }

  /**
   * Get request message for this transaction
   * @return requestMessage as <tt>ModbusMessage</tt>.
   */
  protected ModbusMessage getRequestMsg()
  {
    return fRequestMsg;
  }

  /**
   * Set request message for this transaction
   * @param requestMsg
   */
  protected void setRequestMsg(ModbusMessage requestMsg)
  {
    this.fRequestMsg = requestMsg;
  }

  /**
   * Get response message for this transaction
   * @return responseMessage as <tt>ModbusMessage</tt>
   */
  protected ModbusMessage getResponseMsg()
  {
    return fResponseMsg;
  }

  /**
   * Set response message for this transaction
   * @param responseMsg
   */
  protected void setResponseMsg(ModbusMessage responseMsg)
  {
    this.fResponseMsg = responseMsg;
  }
}

