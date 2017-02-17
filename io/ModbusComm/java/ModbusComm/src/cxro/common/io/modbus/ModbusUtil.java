//@license@
package cxro.common.io.modbus;

/**
 * Helper class that provides utility methods.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 *
 * @version @version@ (@date@)
 */
public final class ModbusUtil
{
  /**
   * Returns the given byte[] as hex encoded string.
   *
   * @param data a byte[] array.
   * @return a hex encoded String.
   */
  public static String toHex(byte[] data)
  {
    return toHex(data, 0, data.length);
  }

  /**
   * Returns a <tt>String</tt> containing unsigned hexadecimal numbers as
   * digits. The <tt>String</tt> will coontain two hex digit characters for each
   * byte from the passed in <tt>byte[]</tt>.<br>
   * The bytes will be separated by a space character.
   * <p/>
   *
   * @param data the array of bytes to be converted into a hex-string.
   * @param off the offset to start converting from.
   * @param length the number of bytes to be converted.
   *
   * @return	the generated hexadecimal representation as <code>String</code>.
   */
  public static String toHex(byte[] data, int off, int length)
  {
    //double size, two bytes (hex range) for one byte
    StringBuilder buf = new StringBuilder(data.length * 2);
    for (int i = off; i < length; i++)
    {
      //don't forget the second hex digit
      if (((int) data[i] & 0xff) < 0x10)
      {
        buf.append("0");
      }
      buf.append(Long.toString((int) data[i] & 0xff, 16));
      if (i < data.length - 1)
      {
        buf.append(" ");
      }
    }
    return buf.toString();
  }

  /**
   * Returns a <tt>byte[]</tt> containing the given byte as unsigned hexadecimal
   * number digits.
   * <p/>
   *
   * @param i the int to be converted into a hex string.
   * @return the generated hexadecimal representation as <code>byte[]</code>.
   */
  public static byte[] toHex(int i)
  {
    StringBuilder buf = new StringBuilder(2);
    //don't forget the second hex digit
    if (((int) i & 0xff) < 0x10)
    {
      buf.append("0");
    }
    buf.append(Long.toString((int) i & 0xff, 16).
      toUpperCase());
    return buf.toString().
      getBytes();
  }

  /**
   * Converts the register (a 16 bit value) into an unsigned short. The value
   * returned is:
   * <p>
   * <pre><code>(((a &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
   * </code></pre>
   * <p/>
   * This conversion has been taken from the documentation of the
   * <tt>DataInput</tt> interface.
   *
   * @param bytes a register as <tt>byte[2]</tt>.
   * @return the unsigned short value as <tt>int</tt>.
   * @see java.io.DataInput
   */
  public static int registerToUnsignedShort(byte[] bytes)
  {
    return ((bytes[0] & 0xff) << 8 | (bytes[1] & 0xff));
  }

  /**
   * Converts the given unsigned short into a register (2 bytes). The byte
   * values in the register, in the order shown, are:
   * <p/>
   * <
   * pre><code>
   * (byte)(0xff &amp; (v &gt;&gt; 8))
   * (byte)(0xff &amp; v)
   * </code></pre>
   * <p/>
   * This conversion has been taken from the documentation of the
   * <tt>DataOutput</tt> interface.
   *
   * @param v
   * @return the register as <tt>byte[2]</tt>.
   * @see java.io.DataOutput
   */
  public static byte[] unsignedShortToRegister(int v)
  {
    byte[] register = new byte[2];
    register[0] = (byte) (0xff & (v >> 8));
    register[1] = (byte) (0xff & v);
    return register;
  }

  /**
   * Converts the given register (16-bit value) into a <tt>short</tt>. The value
   * returned is:
   * <p/>
   * <
   * pre><code>
   * (short)((a &lt;&lt; 8) | (b &amp; 0xff))
   * </code></pre>
   * <p/>
   * This conversion has been taken from the documentation of the
   * <tt>DataInput</tt> interface.
   *
   * @param bytes bytes a register as <tt>byte[2]</tt>.
   * @return the signed short as <tt>short</tt>.
   */
  public static short registerToShort(byte[] bytes)
  {
    return (short) ((bytes[0] << 8) | (bytes[1] & 0xff));
  }

  /**
   * Converts the register (16-bit value) at the given index into a
   * <tt>short</tt>. The value returned is:
   * <p/>
   * <
   * pre><code>
   * (short)((a &lt;&lt; 8) | (b &amp; 0xff))
   * </code></pre>
   * <p/>
   * This conversion has been taken from the documentation of the
   * <tt>DataInput</tt> interface.
   *
   * @param bytes a <tt>byte[]</tt> containing a short value.
   * @param idx an offset into the given byte[].
   * @return the signed short as <tt>short</tt>.
   */
  public static short registerToShort(byte[] bytes, int idx)
  {
    return (short) ((bytes[idx] << 8) | (bytes[idx + 1] & 0xff));
  }

  /**
   * Converts the given <tt>short</tt> into a register (2 bytes). The byte
   * values in the register, in the order shown, are:
   * <p/>
   * <
   * pre><code>
   * (byte)(0xff &amp; (v &gt;&gt; 8))
   * (byte)(0xff &amp; v)
   * </code></pre>
   *
   * @param s
   * @return a register containing the given short value.
   */
  public static byte[] shortToRegister(short s)
  {
    byte[] register = new byte[2];
    register[0] = (byte) (0xff & (s >> 8));
    register[1] = (byte) (0xff & s);
    return register;
  }

  /**
   * Converts a byte[4] binary int value to a primitive int.<br>
   * The value returned is:
   * <p>
   * <pre>
   * <code>
   * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) |
   * &#32;((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
   * </code></pre>
   *
   * @param bytes registers as <tt>byte[4]</tt>.
   * @return the integer contained in the given register bytes.
   */
  public static int registersToInt(byte[] bytes)
  {
    return (((bytes[0] & 0xff) << 24)
            | ((bytes[1] & 0xff) << 16)
            | ((bytes[2] & 0xff) << 8)
            | (bytes[3] & 0xff));
  }

  /**
   * Converts an int value to a byte[4] array.
   *
   * @param v the value to be converted.
   * @return a byte[4] containing the value.
   */
  public static byte[] intToRegisters(int v)
  {
    byte[] registers = new byte[4];
    registers[0] = (byte) (0xff & (v >> 24));
    registers[1] = (byte) (0xff & (v >> 16));
    registers[2] = (byte) (0xff & (v >> 8));
    registers[3] = (byte) (0xff & v);
    return registers;
  }

  /**
   * Converts a byte[8] binary long value into a long primitive.
   *
   * @param bytes a byte[8] containing a long value.
   * @return a long value.
   */
  public static long registersToLong(byte[] bytes)
  {
    return ((((long) (bytes[0] & 0xff) << 56)
             | ((long) (bytes[1] & 0xff) << 48)
             | ((long) (bytes[2] & 0xff) << 40)
             | ((long) (bytes[3] & 0xff) << 32)
             | ((long) (bytes[4] & 0xff) << 24)
             | ((long) (bytes[5] & 0xff) << 16)
             | ((long) (bytes[6] & 0xff) << 8)
             | ((long) (bytes[7] & 0xff))));
  }

  /**
   * Converts a long value to a byte[8].
   *
   * @param v the value to be converted.
   * @return a byte[8] containing the long value.
   */
  public static byte[] longToRegisters(long v)
  {
    byte[] registers = new byte[8];
    registers[0] = (byte) (0xff & (v >> 56));
    registers[1] = (byte) (0xff & (v >> 48));
    registers[2] = (byte) (0xff & (v >> 40));
    registers[3] = (byte) (0xff & (v >> 32));
    registers[4] = (byte) (0xff & (v >> 24));
    registers[5] = (byte) (0xff & (v >> 16));
    registers[6] = (byte) (0xff & (v >> 8));
    registers[7] = (byte) (0xff & v);
    return registers;
  }

  /**
   * Converts a byte[4] binary float value to a float primitive.
   *
   * @param bytes the byte[4] containing the float value.
   * @return a float value.
   */
  public static float registersToFloat(byte[] bytes)
  {
    return Float.intBitsToFloat((((bytes[0] & 0xff) << 24)
                                 | ((bytes[1] & 0xff) << 16)
                                 | ((bytes[2] & 0xff) << 8)
                                 | (bytes[3] & 0xff)));
  }

  /**
   * Converts a float value to a byte[4] binary float value.
   *
   * @param f the float to be converted.
   * @return a byte[4] containing the float value.
   */
  public static byte[] floatToRegisters(float f)
  {
    return intToRegisters(Float.floatToIntBits(f));
  }

  /**
   * Converts a byte[8] binary double value into a double primitive.
   *
   * @param bytes a byte[8] to be converted.
   * @return a double value.
   */
  public static double registersToDouble(byte[] bytes)
  {
    return Double.longBitsToDouble(((((long) (bytes[0] & 0xff) << 56)
                                     | ((long) (bytes[1] & 0xff) << 48)
                                     | ((long) (bytes[2] & 0xff) << 40)
                                     | ((long) (bytes[3] & 0xff) << 32)
                                     | ((long) (bytes[4] & 0xff) << 24)
                                     | ((long) (bytes[5] & 0xff) << 16)
                                     | ((long) (bytes[6] & 0xff) << 8)
                                     | ((long) (bytes[7] & 0xff)))));
  }

  /**
   * Converts a double value to a byte[8].
   *
   * @param d the double to be converted.
   * @return a byte[8].
   */
  public static byte[] doubleToRegisters(double d)
  {
    return longToRegisters(Double.doubleToLongBits(d));
  }

  /**
   * Converts an unsigned byte to an integer.
   *
   * @param b the byte to be converted.
   * @return an integer containing the unsigned byte value.
   */
  public static int unsignedByteToInt(byte b)
  {
    return (int) b & 0xFF;
  }

  /**
   * Returns the low byte of a 16-bit word. The word is stored as a 32-bit int.
   *
   * @param wd
   * @return
   */
  public static byte lowByte(int wd)
  {
    return (new Integer(0xff & wd).byteValue());
  }

  /**
   * Returns the high byte of a 16-bit word. The word is stored as a 32-bit int.
   *
   * @param wd
   * @return
   */
  public static byte hiByte(int wd)
  {
    return (new Integer(0xff & (wd >> 8)).byteValue());
  }

  /**
   * Return a 16-bit word composed of two bytes. Stored inside a 32-bit int.
   *
   * @param hibyte
   * @param lowbyte
   * @return
   */
  public static int makeWord(int hibyte, int lowbyte)
  {
    int hi = 0xFF & hibyte;
    int low = 0xFF & lowbyte;
    return ((hi << 8) | low);
  }
}

