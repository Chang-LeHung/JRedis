package org.jredis.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.util.ArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jredis.JRSerializerUtil;
import org.jredis.JRedisObject;
import org.jredis.command.CommandContainer;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.string.JRString;

@Slf4j
public class JRedisClient {

  @Getter
  private final SocketChannel channel;

  private final JRedisServer server;

  private final ByteBuffer buffer;

  private long lastCommunication;

  private static final int MAX_BUFFER_SIZE = 1024; // maximum bytes of per message

  public enum ClientState {

    IDEL,

    READING,

    WRITING
  }

  @Getter
  private ClientState state;

  private ByteBuffer out;

  public JRedisClient(SocketChannel channel, JRedisServer server) {
    this.channel = channel;
    this.server = server;
    buffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN); // 1k buffer
    lastCommunication = getCurrentSecond();
    state = ClientState.IDEL;
  }

  public void register(int op) throws ClosedChannelException {
    channel.register(server.getSelector(), op);
  }

  public void close()  {
    // keep following order
    server.removeClient(this);
    SelectionKey key = channel.keyFor(server.getSelector());
    // unregister
    key.cancel();

    try {
      channel.close();
    } catch (IOException ignore) {
    }
  }

  private static long getCurrentSecond() {
    return System.currentTimeMillis() / 1000;
  }


  private void updateLastCommunication() {
    lastCommunication = getCurrentSecond();
  }

  public int read() {
    if (state == ClientState.IDEL) {
      updateLastCommunication();
      try{
        int size = channel.read(buffer);
        if (size == -1) {
          close();
        } else if (size < MAX_BUFFER_SIZE) {
          buffer.flip();
          state = ClientState.WRITING;
          handleRequest();
        } else {
          // violate the rule: less than 1k per message
          close();
          return -1;
        }
        return size;
      } catch (IOException ignore) {
        close();
      }
    }
    return 0;
  }

  public int write() {
    if (state == ClientState.WRITING) {
      updateLastCommunication();
      try {
        int s = channel.write(out);
        if (!out.hasRemaining()) {
          out = null;
          state = ClientState.IDEL;
        }
        return s;
      } catch (IOException e) {
        close();
      }
    }
    return 0;
  }

  public boolean checkTimeout() throws IOException {
    long now = getCurrentSecond();
    if (now - lastCommunication > server.getKeepAlive()) {
      close();
      return true;
    }
    return false;
  }

  private void handleRequest() {
    JRedisObject ans = processCommand();
    try {
      out = ByteBuffer.wrap(ans.serialize());
    } catch (JRedisTypeNotMatch e) {
      // this will never happen
      out = ByteBuffer.wrap("JRedisTypeNotMatch error in handleRequest".getBytes());
    }
  }

  private JRedisObject processCommand() {
    try {
      return acceptMsgAndExecuteCommand();
    } catch (JRedisDataBaseException | JRedisTypeNotMatch e) {
      return new JRString(e.getMessage());
    } catch (IOException e) {
      return new JRString("socket channel broken");
    }
  }

  private JRedisObject acceptMsgAndExecuteCommand()
      throws JRedisDataBaseException, JRedisTypeNotMatch, IOException {
    // get which command
    byte command = buffer.get();
    // get message
    int remaining = buffer.remaining();
    byte[] msg = new byte[remaining];
    buffer.get(msg);
    // back to normal state
    buffer.clear();

    var stream = new ByteArrayInputStream(msg);
    var args = new ArrayList<JRedisObject>();
    while (stream.available() != 0) {
      JRedisObject obj = JRSerializerUtil.deserialize(stream);
      args.add(obj);
    }
    var commandAgs = args.toArray(new JRedisObject[0]);
    Socket socket = channel.socket();
    InetAddress ip = socket.getInetAddress();
    log.info("IP: {} port: {} command: {}, args: {}", ip.getHostAddress(), socket.getPort(), CommandContainer.getCommand(command).getName(), commandAgs);
    return server.getActiveDatabase().execute(command, commandAgs);
  }

  public boolean isIDEL() {
    return state == ClientState.IDEL;
  }

  public boolean isWRITING() {
    return state == ClientState.WRITING;
  }

  public boolean isREADING() {
    return state == ClientState.READING;
  }
}
