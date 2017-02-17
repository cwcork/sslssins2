//@license@
package cxro.common.io.modbus;

/**
 * Class that implements a <tt>ModbusIOException</tt>. 
 * Instances of this exception are thrown when errors in the I/O occur.
 *
 * @author Carl Cork
 */
public class ModbusIOException
  extends ModbusException
{
  private boolean m_EOF = false;

  /**
   * Constructs a new <tt>ModbusIOException</tt>
   * instance.
   */
  public ModbusIOException()
  {
  }

  /**
   * Constructs a new <tt>ModbusIOException</tt>
   * instance with the given message.
   * <p>
   * @param message the message describing this
   * <tt>ModbusIOException</tt>.
   */
  public ModbusIOException(String message)
  {
    super(message);
  }

  /**
   * For exception chaining
   *
   * @param ex
   */
  public ModbusIOException(Throwable ex)
  {
    super(ex);
  }

  /**
   * For exception chaining
   *
   * @param message
   * @param ex
   */
  public ModbusIOException(String message, Throwable ex)
  {
    super(message, ex);
  }

  /**
   * Constructs a new <tt>ModbusIOException</tt>
   * instance.
   *
   * @param b true if caused by end of stream, false otherwise.
   */
  public ModbusIOException(boolean b)
  {
    m_EOF = b;
  }

  /**
   * Constructs a new <tt>ModbusIOException</tt>
   * instance with the given message.
   * <p>
   * @param message the message describing this
   * <tt>ModbusIOException</tt>.
   * @param b true if caused by end of stream, false otherwise.
   */
  public ModbusIOException(String message, boolean b)
  {
    super(message);
    m_EOF = b;
  }

  /**
   * Tests if this <tt>ModbusIOException</tt>
   * is caused by an end of the stream.
   * <p>
   * @return true if stream ended, false otherwise.
   */
  public boolean isEOF()
  {
    return m_EOF;
  }

  /**
   * Sets the flag that determines whether this
   * <tt>ModbusIOException</tt> was caused by an end of the stream.
   * <p>
   * @param b true if stream ended, false otherwise.
   */
  public void setEOF(boolean b)
  {
    m_EOF = b;
  }
}

