//@license@
package cxro.common.io.modbus;

/**
 * Class that implements a <tt>ModbusInterruptedException</tt>. 
 * Instances of this exception are thrown when the associated thread is 
 * interrupted during Modbus operations.
 *
 * @author Carl Cork
 */
public class ModbusInterruptedException
  extends ModbusException
{
  /**
   * Constructs a new <tt>ModbusInterruptedException</tt>
   * instance.
   */
  public ModbusInterruptedException()
  {
  }//constructor

  /**
   * Constructs a new <tt>ModbusInterruptedException</tt>
   * instance with the given message.
   * <p>
   * @param message the message describing this
   * <tt>ModbusIOException</tt>.
   */
  public ModbusInterruptedException(String message)
  {
    super(message);
  }//constructor(String)

  /**
   * For exception chaining
   *
   * @param ex
   */
  public ModbusInterruptedException(Throwable ex)
  {
    super(ex);
  }

  /**
   * For exception chaining
   *
   * @param message
   * @param ex
   */
  public ModbusInterruptedException(String message, Throwable ex)
  {
    super(message, ex);
  }
}
