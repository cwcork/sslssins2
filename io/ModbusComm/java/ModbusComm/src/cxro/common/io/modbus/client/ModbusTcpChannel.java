//@license@
package cxro.common.io.modbus.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * Class that implements a Modbus/TCP protocol channel.
 * <p>
 * In addition to managing the socket io channel,
 * <tt>ModbusTcpChannel</tt> is responsible for the ModbusEthernet MBAP header.
 * The full ModbusEthernet ADU message buffer is passed in to this class (as
 * {@link ModbusMessage} references) and the MBAP portion of the buffer is
 * managed by this class. The Modbus PDU portion is managed separately by
 * {@link ModbusMessage}.
 *
 * @author Carl Cork
 */
public class ModbusTcpChannel
  extends ModbusChannel
{
  private SocketChannel sChan;

  /**
   * Constructs a <tt>ModbusChannel</tt> instance with a given destination
   * address.
   *
   * @param adr the destination <tt>InetAddress</tt>.
   */
  public ModbusTcpChannel(InetAddress adr)
  {
    address = adr;
  }

  /**
   * Opens this <tt>ModbusTcpChannel</tt>.
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
    // open the channel, make channel nonblocking, and register with selector
    SocketChannel tsChan = SocketChannel.open();
    tsChan.socket().setTcpNoDelay(true);
    tsChan.configureBlocking(false);
    SelectionKey key = tsChan.register(selector, SelectionKey.OP_READ);

    // initiate connection attempt with timeout
    tsChan.connect(new InetSocketAddress(address, port));
    int period = 0;
    while (!tsChan.finishConnect())
    {
      try
      {
        Thread.sleep(100);
        period += 100;
        if (period > connectTimeout)
        {
          throw new SocketTimeoutException("TCP Connection Timeout");
        }
      }
      catch (InterruptedException ex)
      {
        throw new ClosedByInterruptException();
      }
    }

    // update state
    isConnected = tsChan.isConnected();
    sChan = tsChan;
  }

  @Override
  public void closeSpi()
    throws IOException
  {
    try
    {
      sChan.close();
    }
    finally
    {
      sChan = null;
      isConnected = false;
    }
  }

  @Override
  protected int writeSpi(ByteBuffer sndBuffer)
    throws IOException
  {
    return sChan.write(sndBuffer);
  }

  @Override
  protected int readSpi(ByteBuffer rcvBuffer)
    throws IOException
  {
    return sChan.read(rcvBuffer);
  }

}
