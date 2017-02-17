//@license@
package cxro.common.io.modbus;

/**
 * Interface defining all constants related to the Modbus protocol.
 *
 * @author Dieter Wimberger (original), Carl Cork
 * @version @version@ (@date@)
 */
public interface Modbus
{
  /**
   * Defines the function code FC01 =  <tt>Read Coils</tt>.
   */
  public static final int READ_COILS = 1;
  /**
   * Defines the function code FC02 = <tt>Read Discrete Inputs</tt>.
   */
  public static final int READ_DISCRETE_INPUTS = 2;
  /**
   * Defines the function code FC03 = <tt>Read Holding Registers</tt>.
   */
  public static final int READ_HOLDING_REGISTERS = 3;
  /**
   * Defines the function code FC04 = <tt>Read Input Registers</tt>.
   */
  public static final int READ_INPUT_REGISTERS = 4;
  /**
   * Defines the function code FC05 = <tt>Write Single Coil</tt>.
   */
  public static final int WRITE_SINGLE_COIL = 5;
  /**
   * Defines the function code FC06 = <tt>Write Single Register</tt>.
   */
  public static final int WRITE_SINGLE_REGISTER = 6;
  /**
   * Defines the function code FC15 = <tt>Write Multiple Coils</tt>.
   */
  public static final int WRITE_MULTIPLE_COILS = 15;
  /**
   * Defines the function code FC16 = <tt>Write Multiple Registers</tt>.
   */
  public static final int WRITE_MULTIPLE_REGISTERS = 16;
  /**
   * Defines the function code FC102 = <tt>RPC MESSAGE</tt>.
   */
  public static final int RPC_MESSAGE = 102;
  /**
   * Defines the maximum number of bits in read of multiple input discretes or
   * coils (<b>2000</b>).
   */
  public static final int MAX_READ_BITS = 2000;
  /**
   * Defines the maximum number of bits in write of multiple input discretes or
   * coils (<b>1968</b>).
   */
  public static final int MAX_WRITE_BITS = 1968;
  /**
   * Defines the maximum number of registers in read of multiple registers
   * (<b>125</b>).
   */
  public static final int MAX_READ_REGISTERS = 125;
  /**
   * Defines the maximum number of registers in write of multiple registers
   * (<b>123</b>).
   */
  public static final int MAX_WRITE_REGISTERS = 123;
  /**
   * Defines the Modbus server exception offset that is added to the function
   * code, to flag an exception.
   */
  public static final int EXCEPTION_OFFSET = 0x80;
  /**
   * Defines the Modbus server exception type <tt>illegal function</tt>. This
   * exception code is returned if the server:
   * <ul>
   * <li>does not implement the function code <b>or</b></li>
   * <li>is not in a state that allows it to process the function</li>
   * </ul>
   */
  public static final int ILLEGAL_FUNCTION_EXCEPTION = 1;
  /**
   * Defines the Modbus server exception type <tt>illegal data address</tt>.
   * This exception code is returned if the reference:
   * <ul>
   * <li>does not exist on the server <b>or</b></li>
   * <li>the combination of reference and length exceeds the bounds of the
   * existing registers.
   * </li>
   * </ul>
   */
  public static final int ILLEGAL_ADDRESS_EXCEPTION = 2;
  /**
   * Defines the Modbus server exception type <tt>illegal data value</tt>. This
   * exception code indicates a fault in the structure of the data values of a
   * complex request, such as an incorrect implied length.<br>
   * <b>This code does not indicate a problem with application specific validity
   * of the value.</b>
   */
  public static final int ILLEGAL_VALUE_EXCEPTION = 3;
  /**
   * Defines the Modbus server exception type <tt>SLAVE DEVICE FAILURE</tt>. The
   * server (or slave) is not responding to requested action. This might be due
   * to either a recoverable or unrecoverable fault.
   */
  public static final int SLAVE_DEVICE_FAILURE_EXCEPTION = 4;
  /**
   * Defines the Modbus server exception type <tt>ACKNOWLEDGE</tt>. The server
   * (or slave) has accepted the request and is processing it, but a long
   * duration of time will be required to do so.
   */
  public static final int ACKNOWLEDGE_EXCEPTION = 5;
  /**
   * Defines the Modbus server exception type <tt>SLAVE DEVICE BUSY</tt>. The
   * server (or slave) is engaged in processing a long–duration program command.
   * The client (or master) should retransmit the message later when the server
   * (or slave) is free.
   */
  public static final int SLAVE_DEVICE_BUSY_EXCEPTION = 6;
  /**
   * Defines the default port number of Modbus (=<tt>502</tt>).
   */
  public static final int DEFAULT_PORT = 502;
  /**
   * Defines the default setting for I/O operation timeouts in milliseconds
   * (=<tt>3000</tt>).
   */
  public static final int DEFAULT_TIMEOUT = 3000;
  /**
   * Defines the maximum ADU message length in bytes (=<tt>260</tt>).
   */
  public static final int MAX_MESSAGE_LENGTH = 260;
  /**
   * Defines the default transaction identifier (=<tt>0</tt>).
   */
  public static final int DEFAULT_TRANSACTION_ID = 0;
  /**
   * Defines the default protocol identifier (=<tt>0</tt>).
   */
  public static final int DEFAULT_PROTOCOL_ID = 0;
  /**
   * Defines the default unit identifier (=<tt>0</tt>).
   */
  public static final int DEFAULT_UNIT_ID = 0;
  /**
   * Defines the maximum value of the transaction identifier.
   */
  public static final int MAX_TRANSACTION_ID = (Short.MAX_VALUE * 2) - 1;
}

