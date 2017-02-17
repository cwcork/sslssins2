//@license@
package cxro.common.io.modbus;

/**
 * Class that implements a <tt>ModbusServerException</tt>. Instances of this
 * exception are thrown when the server returns a Modbus message exception.
 *
 * @author Carl Cork
 */
public class ModbusServerException
  extends ModbusException
{
  //instance attributes
  private int m_Type = -1;

  /**
   * Constructs a new <tt>ModbusServerException</tt>
   * instance with the given type.<br>
   * Types are defined according to the protocol specification in
   * <tt>gov.lbl.modbus.Modbus</tt>.
   * <p>
   * @param TYPE the type of exception that occured.
   *
   * @see gov.lbl.modbus.Modbus
   */
  public ModbusServerException(int TYPE)
  {
    super();
    m_Type = TYPE;
  }//constructor

  /**
   * Returns the type of this <tt>ModbusServerException</tt>.
   * <br>
   * Types are defined according to the protocol specification in
   * <tt>gov.lbl.modbus.Modbus</tt>.
   * <p>
   * @return the type of this <tt>ModbusServerException</tt>.
   *
   * @see gov.lbl.modbus.Modbus
   */
  public int getType()
  {
    return m_Type;
  }//getType

  /**
   * Tests if this <tt>ModbusServerException</tt>
   * is of a given type.
   * <br>
   * Types are defined according to the protocol specification in
   * <tt>gov.lbl.modbus.Modbus</tt>.
   * <p>
   * @param TYPE the type to test this
   * <tt>ModbusServerException</tt> type against.
   *
   * @return true if this <tt>ModbusServerException</tt>
   * is of the given type, false otherwise.
   *
   * @see gov.lbl.modbus.Modbus
   */
  public boolean isType(int TYPE)
  {
    return (TYPE == m_Type);
  }//isType

  @Override
  public String getMessage()
  {
    return "Error Code = " + m_Type;
  }//getMessage
}//ModbusServerException

