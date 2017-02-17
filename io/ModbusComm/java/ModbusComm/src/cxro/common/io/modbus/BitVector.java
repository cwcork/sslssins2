//@license@
package cxro.common.io.modbus;

/**
 * Class that implements a collection for bits, storing them packed into bytes.
 * The access operations will index from the LSB (rightmost) bit.
 *
 * @author Dieter Wimberger(original), Carl Cork
 */
public final class BitVector
{
  private byte[] fBits;
  private int fSize;

  /**
   * Constructs a vector capable of holding size bits.
   */
  public BitVector(int size)
  {
    if (size < 1)
    {
      throw new IllegalArgumentException("n must be >= 1");
    }
    fSize = size;
    fBits = new byte[((size - 1) >> 3) + 1];
  }

  /**
   * Construct a vector by wrapping a byte array.
   * Assume all bits are part of BitVector.
   * @param data a byte[] containing packed bits.
   * @return the newly created <tt>BitVector</tt> instance.
   */
  public BitVector(byte[] data)
  {
    fBits = data;
    fSize = data.length << 3;
  }

  /**
   * Construct a vector by wrapping a byte array.
   *
   * @param data a byte[] containing packed bits.
   * @param size number of bits in vector.
   */
  public BitVector(byte[] data, int size)
  {
    this(data);
    forceSize(size);
  }

  /**
   * Sets the value of bit at index position to one.
   * @param index bit position in vector.
   */
  public final void set(int index)
  {
    fBits[index >> 3] |= 1 << (index & 7);
  }

  /**
   * Sets the value of bit at index position is set to zero.
   * @param index bit position in vector.
   */
  public final void clear(int index)
  {
    fBits[index >> 3] &= ~(1 << (index & 7));
  }

  /**
   * Returns true if bit at index position is one and false if it is zero.
   * @param index
   * @return true if bit is set
   */
  public final boolean get(int index)
  {
    return (fBits[index >> 3] & (1 << (index & 7))) != 0;
  }

  /**
   * Returns the number of bits in this vector. This is also one greater than
   * the number of the largest valid bit number.
   * @return size 
   */
  public final int size()
  {
    return fSize;
  }

  /**
   * Returns the <tt>byte[]</tt> which is used to store the bits of this
   * <tt>BitVector</tt>.
   * <p>
   * @return the <tt>byte[]</tt> used to store the bits.
   */
  public final byte[] getBytes()
  {
    return fBits;
  }

  /**
   * Sets the <tt>byte[]</tt> which stores the bits of this <tt>BitVector</tt>.
   * <p>
   * @param aData a <tt>byte[]</tt>.
   */
  public final void setBytes(byte[] aData)
  {
    System.arraycopy(aData, 0, fBits, 0, aData.length);
  }

  /**
   * Sets the <tt>byte[]</tt> which stores the bits of this <tt>BitVector</tt>.
   * <p>
   * @param aData a <tt>byte[]</tt> array containing bitmap.
   * @param aSize number of bits mapped in aData array.
   */
  public final void setBytes(byte[] aData, int aSize)
  {
    System.arraycopy(aData, 0, fBits, 0, aData.length);
    this.fSize = aSize;
  }

  /**
   * Returns the state of the bit at the given index of this
   * <tt>BitVector</tt>.
   * <p>
   * @param index the index of the bit to be returned.
   *
   * @return true if the bit at the specified index is set, false otherwise.
   *
   * @throws IndexOutOfBoundsException if the index is out of bounds.
   */
  public final boolean getBit(int index)
  {
    return get(index);
  }

  /**
   * Sets the state of the bit at the given index of this <tt>BitVector</tt>.
   * <p>
   * @param index the index of the bit to be set.
   * @param b true if the bit should be set, false if it should be reset.
   *
   * @throws IndexOutOfBoundsException if the index is out of bounds.
   */
  public final void setBit(int index, boolean b)
  {
    if (b)
    {
      set(index);
    }
    else
    {
      clear(index);
    }
  }

  /**
   * Forces the number of bits in this <tt>BitVector</tt>.
   *
   * @param aSize
   * @throws IllegalArgumentException if the size exceeds the byte[] store size
   * multiplied by 8.
   */
  public final void forceSize(int aSize)
  {
    if (aSize > (fBits.length << 3))
    {
      throw new IllegalArgumentException("Size exceeds byte[] store.");
    }
    else
    {
      this.fSize = aSize;
    }
  }

  /**
   * Returns the number of bytes used to store the collection of bits as
   * <tt>int</tt>.
   * <p>
   * @return the number of bytes used to map this <tt>BitVector</tt>.
   */
  public final int byteSize()
  {
    return fBits.length;
  }

  /**
   * Returns a <tt>String</tt> representing the contents of the bit collection
   * in a way that can be printed to a screen or log.
   * <p>
   *
   * @return a <tt>String</tt> representing this <tt>BitVector</tt>.
   */
  @Override
  public String toString()
  {
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < size(); i++)
    {
      sbuf.append(get(i) ? '1' : '0');
      if (((i + 1) % 8) == 0)
      {
        sbuf.append(" ");
      }
    }
    return sbuf.toString();
  }

  public static void main(String[] args)
  {
    System.out.println("BitVector: Create vector with 24 elements");
    BitVector test = new BitVector(24);

    System.out.println("BitVector: Clear bit 6, Set bit 7");
    test.set(6);
    test.clear(6);
    test.set(7);
    System.out.println("BitVector: Verify,"
                       + " bit 6 = " + test.get(6)
                       + " bit 7 = " + test.get(7));

    System.out.println("BitVector: "
                       + "Set bits 0, 2, 3, 6, 8, 10; Clear bit 7");
    test.setBit(6, true);
    test.setBit(3, true);
    test.setBit(2, true);
    test.setBit(0, true);
    test.setBit(7, false);
    test.setBit(8, true);
    test.setBit(10, true);
    System.out.println("BitVector: Verify: " + test);

    System.out.println("BitVector: get backing array, reload and verify");
    test = new BitVector(test.getBytes(), test.size());
    System.out.println("BitVector: Verify: " + test);

    System.out.println("BitVector: bytes toHex "
                       + ModbusUtil.toHex(test.getBytes()));
  }
}

