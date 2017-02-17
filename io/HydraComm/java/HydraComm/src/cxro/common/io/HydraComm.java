// @license
package cxro.common.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author cwcork
 */
public class HydraComm
{
  private static final Logger logger = Logger.getLogger(HydraComm.class.getName());
  private static final Charset ASCII = Charset.forName("US-ASCII");
  private static final int DEFAULT_TIMEOUT = 1000;
  private static final int DEFAULT_PORT = 400;
  private static final int MAX_MESSAGE_LENGTH = 1024;
  //
  private final ByteBuffer in = ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH);
  private final ByteBuffer out = ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH);
  
  private String hostName;
  private SocketChannel chan;
  private InetAddress address;
  private int port = DEFAULT_PORT;
  private SynchronousQueue<String> queue;
  private ExecutorService exec;
  private boolean shutdown = false;
  private boolean reconnect = false;

  //-- constructors
  public HydraComm(String hostName)
  throws UnknownHostException, IOException
  {
    // use default port
    this(hostName, HydraComm.DEFAULT_PORT);
  }

  public HydraComm(String hostName, int port)
  throws UnknownHostException, IOException
  {
    this.hostName = hostName;
    this.port = port;

    address = InetAddress.getByName(this.hostName);
    chan = SocketChannel.open();
    chan.socket().setTcpNoDelay(false);
    chan.connect(new InetSocketAddress(address, port));
    chan.finishConnect();

    shutdown = false;
    reconnect = false;
    queue = new SynchronousQueue<>();
    exec = Executors.newSingleThreadExecutor();
    exec.execute(new HydraComm.Reader());
  }

  //-- public --
  public String getHostName()
  {
    return hostName;
  }

  public synchronized void snd(String msg)
  throws IOException
  {
    try
    {
      out.clear();
      out.put(ASCII.encode(msg));
      out.flip();
      chan.write(out);
    }
    catch (ClosedChannelException ex)
    {
      // unblock reader and let it try to reconnect
      queue.poll();
      throw new IOException("ClosedChannelException");
    }
  }

  public synchronized String rcv(long timeout, TimeUnit unit)
  throws IOException, InterruptedException
  {
    String res = queue.poll(timeout, unit);
    if (res == null)
    {
      // unblock reader and let it try to reconnect
      queue.poll();
      throw new InterruptedException("Read timeout");
    }
    return (res);
  }

  public synchronized String sar(String command)
  throws IOException, InterruptedException
  {
    return sar(command, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
  }

  public synchronized String sar(String msg, int timeout, TimeUnit unit)
  throws IOException, InterruptedException
  {
    this.snd(msg);
    return (this.rcv(timeout, unit));
  }

  public void close()
  throws IOException
  {
    try
    {
      shutdown = true;
      exec.shutdownNow();
      exec.awaitTermination(100, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException ex)
    {
      // ignore
    }
  }

  private class Reader
  implements Runnable
  {
    private Reader()
    {
    }

    @Override
    public void run()
    {
      try
      {
        while (!shutdown)
        {
          try
          {
            // Reader has responsibility of reconnect
            if (reconnect || !chan.isConnected())
            {
              chan.close();
              chan = SocketChannel.open();
              chan.socket().setTcpNoDelay(false);
              chan.connect(new InetSocketAddress(address, port));
              chan.finishConnect();
              reconnect = false;
            }
            in.clear();
            chan.read(in);
            in.flip();
            queue.put(ASCII.decode(in).toString());
          }
          catch (ClosedByInterruptException ex)
          {
            logger.log(Level.INFO, null, ex);
          }
          catch (ClosedChannelException ex)
          {
            logger.log(Level.SEVERE, null, ex);
          }
          catch (IOException ex)
          {
            logger.log(Level.SEVERE, null, ex);
            reconnect = true;
          }
        }
      }
      catch (InterruptedException ex)
      {
        logger.log(Level.INFO, null, ex);
      }
      finally
      {
        //System.out.println("Finally hit.");
        if (!shutdown)
        {
          // try to reincarnate
          reconnect = true;
          exec = Executors.newSingleThreadExecutor();
          exec.execute(new HydraComm.Reader());
        }
      }
    }
  }

  @Override
  protected void finalize()
  throws Throwable
  {
    chan.socket().close();
    chan.close();
    exec.shutdownNow();
    super.finalize();
  }
}
