/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cxro.common.io.modbus.client;

import cxro.common.io.modbus.Modbus;
import cxro.common.io.modbus.ModbusException;
import cxro.common.io.modbus.ModbusIOException;
import cxro.common.io.modbus.ModbusInterruptedException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cwcork
 */
public abstract class ModbusChannel
{
  protected static final Logger logger = Logger.getLogger("gov.lbl.modbus.client.ModbusChannel");
  
  static
  {
    logger.setLevel(null);
  }
  //
  //channel attributes
  protected InetAddress address;
  protected int port = Modbus.DEFAULT_PORT;
  protected int unitId = Modbus.DEFAULT_UNIT_ID;
  protected int connectTimeout = Modbus.DEFAULT_TIMEOUT;
  protected int executeTimeout = Modbus.DEFAULT_TIMEOUT;
  protected int transactionId = Modbus.DEFAULT_TRANSACTION_ID;
  //
  protected Selector selector;
  protected boolean isConnected;
  protected ReentrantLock rLock = new ReentrantLock();

  public ModbusChannel()
  {
  }

  /** CONNECTION MANAGEMENT *************************************************/
  /**
   * Returns the destination <tt>InetAddress</tt> of this <tt>ModbusChannel</tt>.
   *
   * @return the destination address as <tt>InetAddress</tt>.
   */
  public InetAddress getAddress()
  {
    return address;
  }

  /**
   * Sets the destination <tt>InetAddress</tt> of this <tt>ModbusChannel</tt>.
   *
   * @param adr the destination address as <tt>InetAddress</tt>.
   */
  public void setAddress(InetAddress adr)
  {
    address = adr;
  }

  /**
   * Returns the destination port of this <tt>ModbusChannel</tt>.
   *
   * @return the port number as <tt>int</tt>.
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Sets the destination port of this <tt>ModbusChannel</tt>.
   * The default is defined as <tt>Modbus.DEFAULT_PORT</tt>.
   *
   * @param port the port number as <tt>int</tt>.
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Gets the unit identifier for this <tt>ModbusChannel</tt>.
   * <p>
   * The identifier should be a 1-byte non negative
   * integer value valid in the range of 0-255.
   * @return unit id [0..255]
   */
  public int getUnitID()
  {
    return unitId;
  }

  /**
   * Sets the unit identifier for this <tt>ModbusChannel</tt>.
   * <p>
   * The identifier should be a 1-byte non negative
   * integer value valid in the range of 0-255.
   *
   * @param num the unit identifier number to be set, [0..255].
   */
  public void setUnitID(int num)
  {
    unitId = num & 0xff;
  }

  /** 
   * Returns the connection timeout for this <tt>ModbusChannel</tt>.
   * 
   * @return the timeout in milliseconds.
   */
  public int getConnectTimeout()
  {
    return connectTimeout;
  }

  /**
   * Sets the connection timeout for this <tt>ModbusChannel</tt>.
   * 
   * @param connectTimeout the timeout in milliseconds.
   */
  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Returns the execution timeout for this <tt>ModbusChannel</tt>.
   *
   * @return the timeout in milliseconds.
   */
  public int getTimeout()
  {
    return executeTimeout;
  }

  /**
   * Sets the execution timeout for this <tt>ModbusChannel</tt>.
   *
   * @param timeout the timeout in milliseconds.
   */
  public void setTimeout(int timeout)
  {
    executeTimeout = timeout;
  }

  /**
   * Opens this <tt>ModbusChannel</tt> with current timeout.
   * Uses the current value for connectTimeout.
   * @throws java.io.IOException             - if there is a network failure.
   * @throws java.net.SocketTimeoutException - if timeout expires before connecting
   * @throws ClosedByInterruptException      - If another thread interrupts the current thread while
   *                                         the connect operation is in progress, thereby closing 
   *                                         the channel and setting the current thread's 
   *                                         interrupt status
   */
  public void connect()
    throws IOException
  {
    connect(connectTimeout);
  }

  /**
   * Opens this <tt>ModbusChannel</tt> with given timeout.
   * Uses the given timeout value. The currentTimeout value is updated to use this value.
   * @param timeout connection timeout value in milliseconds.
   * @throws java.io.IOException             - if there is a network failure.
   * @throws java.net.SocketTimeoutException - if timeout expires before connecting
   * @throws ClosedByInterruptException      - If another thread interrupts the current thread while
   *                                         the connect operation is in progress, thereby closing 
   *                                         the channel and setting the current thread's 
   *                                         interrupt status
   */
  public void connect(int timeout)
    throws IOException
  {
    if (!isConnected)
    {
      connectTimeout = timeout;
      selector = Selector.open();
      connectSpi(timeout);
    }
  }
  /**
   * Closes this <tt>ModbusChannel</tt>.
   */
  public void close()
  {
    if (isConnected)
    {
      try
      {
        closeSpi();
      }
      catch (IOException ex)
      {
        if (logger.isLoggable(Level.FINE))
        {
          logger.log(Level.FINE, "ModbusChannel: close() failed");
        }
      }
      finally
      {
        selector = null;
      }
    }
  }

  /**
   * Tests if this <tt>ModbusChannel</tt> is connected.
   *
   * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
   */
  public boolean isConnected()
  {
    return isConnected;
  }

  /** MESSAGE HANDLING ******************************************************/
  /**
   * Perform request/response transaction.<p>
   * Locks the <tt>ModbusChannel</tt> for sending
   * an initial <tt>requestMsg</tt> and reading the
   * related <tt>responseMsg</tt>.
   * <p>
   *
   * @param requestMsg Request Message
   * @return responseMsg - Response Message
   * @throws ModbusException if an I/O error occurs,
   *         or the response is a modbus protocol exception.
   */
  public ModbusMessage execute(ModbusMessage requestMsg)
    throws ModbusException
  {
    ModbusMessage responseMsg = null;
    //1. Lock the transaction
    rLock.lock();
    try
    {
      //2. open the connection if not connected
      if (!isConnected)
      {
        try
        {
          connect();
        }
        catch (ClosedByInterruptException ex)
        {
          isConnected = false;
          throw new ModbusInterruptedException(ex);
        }
        catch (IOException ex)
        {
          close();
          throw new ModbusIOException("Connecting failed.");
        }
      }
      //3. Perform transaction, no retries
      //increment transaction id
      transactionId++;
      // write request
      sendRequest(requestMsg);
      //read response message
      responseMsg = receiveResponse();
    }
    finally
    {
      //4. Unlock the transaction
      rLock.unlock();
    }
    return responseMsg;
  }
  
  /**
   * Send modbus request message.<p>
   * <tt>ModbusChannel</tt> is responsible for setting the following fields:<br>
   * <tt>Protocol_ID</tt> - Modbus.DEFAULT_PROTOCOL_ID = 0
   * <tt>Length</tt> - byte length of message following this field
   * <tt>Unit_ID</tt> - default is Modbus.DEFAULT_UNIT_ID = 0
   *
   * @param msg
   * @throws ModbusInterruptedException
   * @throws ModbusIOException
   */
  private void sendRequest(ModbusMessage msg)
    throws ModbusIOException, ModbusInterruptedException
  {
    try
    {
      ByteBuffer sndBuffer = msg.getBuffer();
      int msgLen = sndBuffer.limit();
      // Check for minimum message length (header plus function code)
      if (msgLen < 8)
      {
        throw new ModbusIOException("sendRequest: message too short.");
      }
      // Fill in MBAP header
      sndBuffer.putChar(0, (char) transactionId);
      sndBuffer.putChar(2, (char) Modbus.DEFAULT_PROTOCOL_ID);
      sndBuffer.putChar(4, (char) (msgLen - 6));
      sndBuffer.put(6, (byte) unitId);
      // Write to socket channel
      if (logger.isLoggable(Level.FINE))
      {
        logger.log(Level.FINE, toHexString(sndBuffer));
      }
      writeSpi(sndBuffer);
    }
    catch (ClosedChannelException ex)
    {
      isConnected = false;
      throw new ModbusInterruptedException(ex);
    }
    catch (IOException ex)
    {
      close();
      throw new ModbusIOException("sendRequest: write failed.");
    }
  }

  /**
   * Receive modbus response message
   *
   * @return msg
   * @throws ModbusInterruptedException
   * @throws ModbusIOException
   */
  private ModbusMessage receiveResponse()
    throws ModbusIOException, ModbusInterruptedException
  {
    try
    {
      int readyChannels = selector.select(executeTimeout);
      
      if (readyChannels == 0)
      {
        // timeout or interrupted
        close();
        throw new ModbusInterruptedException("receiveResponse: Timeout or Interrupted");
      }
      
      Set<SelectionKey> selectedKeys = selector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      ByteBuffer rcvBuffer = ByteBuffer.allocateDirect(Modbus.MAX_MESSAGE_LENGTH);
      while (keyIterator.hasNext())
      {
        SelectionKey key = keyIterator.next();
        
        if (key.isReadable())
        {
          //get response (blocking)
          rcvBuffer.clear();
          if (readSpi(rcvBuffer) == -1)
          {
            close();
            throw new ModbusIOException("receiveResponse: Premature end of stream.");
          }
          rcvBuffer.flip();
        }
        
        keyIterator.remove();
      }
      
      //check message length
      if (rcvBuffer.getChar(4) != (rcvBuffer.limit() - 6))
      {
        throw new ModbusIOException("receiveResponse: Bad message length.");
      }
      
      if (logger.isLoggable(Level.FINE))
      {
        logger.log(Level.FINE, toHexString(rcvBuffer));
      }
      
      // else return buffer wrapped as Modbus message
      return new ModbusMessage(rcvBuffer);
    }
    catch (ClosedChannelException ex)
    {
      isConnected = false;
      throw new ModbusInterruptedException(ex);
    }
    catch (IOException ex)
    {
      close();
      throw new ModbusIOException("receiveResponse: IOException.");
    }
  }
  
  protected abstract void connectSpi(int timeout)
    throws IOException;
  
  protected abstract void closeSpi()
    throws IOException;
  
  protected abstract int writeSpi(ByteBuffer sndBuffer)
    throws IOException;
  
  protected abstract int readSpi(ByteBuffer rcvBuffer)
    throws IOException;
  
  /**
   * Returns the this message as hexadecimal string.
   *
   * @param aBuffer
   * @return the message as hex encoded string.
   */
  protected String toHexString(ByteBuffer aBuffer)
  {
    //double size, two bytes (hex range) for one byte
    StringBuilder buf = new StringBuilder(aBuffer.limit());
    for (int i = 0; i < aBuffer.limit(); i++)
    {
      int b = aBuffer.get(i) & 0xff;
      //don't forget the second hex digit
      if (b < 0x10)
      {
        buf.append("0");
      }
      buf.append(Integer.toHexString(b));
      if (i < (aBuffer.limit() - 1))
      {
        buf.append(" ");
      }
    }
    return buf.toString();
  }
}
