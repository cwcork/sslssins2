//@license@
package cxro.common.io.modbus;

/**
 * Superclass of all specialised exceptions in this package.
 *
 * @author Dieter Wimberger(original), Carl Cork
 */
public class ModbusException
  extends Exception
{
  /**
   * Constructs a new <tt>ModbusException</tt> instance.
   */
  public ModbusException()
  {
    super();
  }

  /**
   * Constructs a new <tt>ModbusException</tt> instance with the given message.
   * <p>
   * @param message the message describing this
   * <tt>ModbusException</tt>.
   */
  public ModbusException(String message)
  {
    super(message);
  }

  /**
   * For message chaining
   *
   * @param ex Causal exception
   */
  public ModbusException(Throwable ex)
  {
    super(ex);
  }

  /**
   * For message chaining
   *
   * @param message Description
   * @param ex causal exception
   */
  public ModbusException(String message, Throwable ex)
  {
    super(message, ex);
  }
}

