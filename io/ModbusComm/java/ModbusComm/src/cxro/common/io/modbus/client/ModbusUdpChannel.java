//@license@
package cxro.common.io.modbus.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

/**
 * Class that implements a Modbus/UDP protocol channel.
 * <p>
 * In addition to managing the socket io channel,
 * <tt>ModbusUdpChannel</tt> is responsible for the ModbusEthernet MBAP header.
 * The full ModbusEthernet ADU message buffer is passed in to this class (as
 * {@link ModbusMessage} references) and the MBAP portion of the buffer is
 * managed by this class. The Modbus PDU portion is managed separately by
 * {@link ModbusMessage}.
 *
 * @author Carl Cork
 */
public class ModbusUdpChannel
  extends ModbusChannel
{
  private DatagramChannel uChan;

  /**
   * Constructs a <tt>ModbusChannel</tt> instance with a given destination
   * address.
   *
   * @param adr the destination <tt>InetAddress</tt>.
   */
  public ModbusUdpChannel(InetAddress adr)
  {
    address = adr;
  }

  /**
   * Opens this <tt>ModbusUdpChannel</tt>.
   *
   * @param timeout the timeout in milliseconds
   * @throws java.io.IOException             - if there is a network failure.
   * @throws java.net.SocketTimeoutException - if timeout expires before connecting
   * @throws ClosedByInterruptException      - If another thread interrupts the current thread while
   *                                         the connect operation is in progress, thereby closing
   *                                         the channel and setting the current thread's
   *                                         interrupt status
   */
  @Override
  public synchronized void connectSpi(int timeout)
    throws IOException
  {
    // All servers must support TCP as well as UDP
    // Use this fact to first check for available HOST connection
    try (Socket socket = new Socket())
    {
      socket.connect(new InetSocketAddress(address, port), timeout);
      socket.close();
    }
    catch (IOException ex)
    {
      throw new SocketTimeoutException("HOST Connection Timeout");
    }

    // open the channel, make channel nonblocking, and register with selector
    DatagramChannel tuChan = DatagramChannel.open();
    tuChan.configureBlocking(false);
    SelectionKey key = tuChan.register(selector, SelectionKey.OP_READ);

    // initiate connection attempt with timeout
    // NOTE: unfortunately, this does not check for physical connection to the device
    tuChan.connect(new InetSocketAddress(address, port));
    int period = 0;
    while (!tuChan.isConnected())
    {
      try
      {
        Thread.sleep(100);
        period += 100;
        if (period > connectTimeout)
        {
          tuChan.close();
          throw new SocketTimeoutException("UDP Connection Timeout");
        }
      }
      catch (InterruptedException ex)
      {
        tuChan.close();
        throw new ClosedByInterruptException();
      }
    }

    // update state
    isConnected = tuChan.isConnected();
    uChan = tuChan;
  }

  @Override
  public void closeSpi()
    throws IOException
  {
    try
    {
      uChan.close();
    }
    finally
    {
      uChan = null;
      isConnected = false;
    }
  }

  @Override
  protected int writeSpi(ByteBuffer sndBuffer)
    throws IOException
  {
    return uChan.write(sndBuffer);
  }

  @Override
  protected int readSpi(ByteBuffer rcvBuffer)
    throws IOException
  {
    return uChan.read(rcvBuffer);
  }
}
