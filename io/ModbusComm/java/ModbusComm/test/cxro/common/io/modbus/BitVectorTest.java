/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cxro.common.io.modbus;

import cxro.common.io.modbus.BitVector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cwcork
 */
public class BitVectorTest {

    public BitVectorTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

  /**
   * Test of set method, of class BitVector.
   */
  @Test
  public void testSet()
  {
    System.out.println("set");
    int aBit = 0;
    BitVector instance = null;
    instance.set(aBit);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of clear method, of class BitVector.
   */
  @Test
  public void testClear()
  {
    System.out.println("clear");
    int aBit = 0;
    BitVector instance = null;
    instance.clear(aBit);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of get method, of class BitVector.
   */
  @Test
  public void testGet()
  {
    System.out.println("get");
    int aBit = 0;
    BitVector instance = null;
    boolean expResult = false;
    boolean result = instance.get(aBit);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of size method, of class BitVector.
   */
  @Test
  public void testSize()
  {
    System.out.println("size");
    BitVector instance = null;
    int expResult = 0;
    int result = instance.size();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getBytes method, of class BitVector.
   */
  @Test
  public void testGetBytes()
  {
    System.out.println("getBytes");
    BitVector instance = null;
    byte[] expResult = null;
    byte[] result = instance.getBytes();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setBytes method, of class BitVector.
   */
  @Test
  public void testSetBytes_byteArr()
  {
    System.out.println("setBytes");
    byte[] aData = null;
    BitVector instance = null;
    instance.setBytes(aData);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setBytes method, of class BitVector.
   */
  @Test
  public void testSetBytes_byteArr_int()
  {
    System.out.println("setBytes");
    byte[] aData = null;
    int aSize = 0;
    BitVector instance = null;
    instance.setBytes(aData, aSize);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getBit method, of class BitVector.
   */
  @Test
  public void testGetBit()
  {
    System.out.println("getBit");
    int index = 0;
    BitVector instance = null;
    boolean expResult = false;
    boolean result = instance.getBit(index);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setBit method, of class BitVector.
   */
  @Test
  public void testSetBit()
  {
    System.out.println("setBit");
    int index = 0;
    boolean b = false;
    BitVector instance = null;
    instance.setBit(index, b);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of forceSize method, of class BitVector.
   */
  @Test
  public void testForceSize()
  {
    System.out.println("forceSize");
    int aSize = 0;
    BitVector instance = null;
    instance.forceSize(aSize);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of byteSize method, of class BitVector.
   */
  @Test
  public void testByteSize()
  {
    System.out.println("byteSize");
    BitVector instance = null;
    int expResult = 0;
    int result = instance.byteSize();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class BitVector.
   */
  @Test
  public void testToString()
  {
    System.out.println("toString");
    BitVector instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of main method, of class BitVector.
   */
  @Test
  public void testMain()
  {
    System.out.println("main");
    String[] args = null;
    BitVector.main(args);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}