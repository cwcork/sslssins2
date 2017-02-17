//@license@
package cxro.common.io.modbus.client;

import java.nio.ByteBuffer;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;

/**
 * Manage the Modbus Protocol Data Unit (PDU).<p>
 * This class actually encapsulates the full ModbusTCP 
 * Application Data Unit (ADU = TCP_Header + PDU).
 * However, <tt>ModbusMessage</tt> is only responsible for
 * the Modbus PDU portion of the ADU. The ModbusTCP header
 * is the responsibility of {@link ModbusChannel}.
 *
 * @author Carl Cork
 */
public final class ModbusMessage
{
  //instance fields
  private ByteBuffer m_buffer =
    ByteBuffer.allocateDirect(Modbus.MAX_MESSAGE_LENGTH);

  /**
   * Constructs a new <tt>ModbusMessage</tt> instance,
   * uses default header values.
   * <p>
   */
  public ModbusMessage()
  {
    setFunctionCode(0);
    m_buffer.limit(8);
  }

  /**
   * Constructs a new <tt>ModbusMessage</tt> instance,
   * using whole modbus message array.
   * <p>
   * @param msg Modbus ADU message buffer.
   */
  ModbusMessage(ByteBuffer msg)
  {
    m_buffer = msg;
  }

  /** Header methods ********************************************************/
  /**
   * Gets the function code of  this
   * <tt>ModbusMessage</tt>.<p>
   * The identifier should be a 1-byte non negative
   * integer value valid in the range of 0-255.
   * <p>
   * @return
   */
  public int getFunctionCode()
  {
    return (m_buffer.get(7) & 0xff);
  }

  /**
   * Sets the function code of  this
   * <tt>ModbusMessage</tt>.<br>
   * The identifier should be a 1-byte non negative
   * integer value valid in the range of 0-255.
   *
   * @param num the unit identifier number to be set.
   */
  public void setFunctionCode(int num)
  {
    m_buffer.put(7, (byte) (num & 0xff));
  }

  /**
   * Test for ModbusServerException
   *
   * @return true if exception
   * @throws gov.lbl.modbus.ModbusException
   */
  public boolean isException()
    throws ModbusException
  {
    if (m_buffer.limit() > 7)
    {
      return ((m_buffer.get(7) & 0x80) != 0);
    }
    else
    {
      throw new ModbusException("Illegal Modbus Message");
    }
  }

  /**
   * Get modbus response exception code
   *
   * @return exception code (0..255)
   * @throws gov.lbl.modbus.ModbusException
   */
  public int getExceptionCode()
    throws ModbusException
  {
    if (m_buffer.limit() == 9)
    {
      return (m_buffer.get(8) & 0xff);
    }
    else
    {
      throw new ModbusException("Illegal Modbus Exception Message");
    }
  }

  /*** Data Methods *********************************************************/
  /**
   * Gets the message data from this <tt>ModbusMessage</tt>.<br>
   * The message data corresponds to the remaining bytes following the
   * function code in a modbus message (up to 252 bytes).
   * <p>
   * @return message data as <tt>ByteBuffer</tt>.
   */
  public ByteBuffer getData()
  {
    m_buffer.position(8);
    return m_buffer.slice();
  }

  /**
   * Sets the message data from this <tt>ModbusMessage</tt>.<br>
   * The message data corresponds to the remaining bytes following the
   * function code in a modbus message (up to 252 bytes).
   * <p>
   * @param message data as <tt>ByteBuffer</tt>.
   */
  public void setData(ByteBuffer data)
  {
    m_buffer.clear();       //resets position and limit
    m_buffer.position(8);   //start of data
    m_buffer.put((ByteBuffer) data.position(0));
    m_buffer.flip();        //sets limit to end of data
  }

  /**
   * Get the length of the payload message data.<p>
   * This corresponds to the MBAP_LENGTH minus the UNIT_ID and FUNCTION_CODE
   * bytes.
   * <p>
   * @return the payload data length as <tt>int</tt>.
   */
  public int getDataLength()
  {
    return (m_buffer.limit() - 8);
  }

  /** Utility Methods *******************************************************/
  /**
   * Returns this message as hexadecimal string.
   * Only return the PDU portion of the message.
   * Ignore the MBAP portion which is managed
   * by {@link ModbusChannel}.
   *
   * @return the message as hex encoded string.
   */
  public String toHexString()
  {
    //double size, two bytes (hex range) for one byte
    StringBuilder buf = new StringBuilder(m_buffer.limit());
    for (int i = 7; i < m_buffer.limit(); i++)
    {
      int b = (m_buffer.get(i) & 0xff);
      //don't forget the second hex digit
      if (b < 0x10)
      {
        buf.append("0");
      }
      buf.append(Integer.toHexString(b));
      if (i < (m_buffer.limit() - 1))
      {
        buf.append(" ");
      }
    }
    return buf.toString();
  }

  /** Package private *******************************************************/
  /**
   * Get message buffer for this <tt>ModbusMessage</tt>.<p>
   * Used for raw access to message buffer.
   * <p>
   * @return message buffer
   */
  ByteBuffer getBuffer()
  {
    return (ByteBuffer) m_buffer.position(0);
  }

  /**
   * Set message buffer for this <tt>ModbusMessage</tt>.<p>
   * Used for raw access to message buffer.
   * <p>
   *
   * @param msg message backing array
   */
  void setBuffer(ByteBuffer msg)
  {
    m_buffer = (ByteBuffer) msg.position(0);
  }
}
