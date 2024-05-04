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
import org.jredis.command.Command;
import org.jredis.command.CommandContainer;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.server.middlewares.JRedisRequest;
import org.jredis.string.JRString;

@Slf4j
public class JRedisClient {

  private static final int MAX_BUFFER_SIZE = 1 << 12; // maximum bytes of per message

  @Getter private final SocketChannel channel;

  private final JRedisServer server;

  private final ByteBuffer buffer;

  @Getter private final InetAddress ip;

  @Getter private final int port;

  private long lastCommunication;

  @Getter private ClientState state;

  private ByteBuffer out;

  public JRedisClient(SocketChannel channel, JRedisServer server) {
    this.channel = channel;
    this.server = server;
    buffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN); // 1k buffer
    lastCommunication = getCurrentSecond();
    state = ClientState.IDEL;
    Socket socket = channel.socket();
    ip = socket.getInetAddress();
    port = socket.getPort();
  }

  private static long getCurrentSecond() {
    return System.currentTimeMillis() / 1000;
  }

  public void register(int op) throws ClosedChannelException {
    channel.register(server.getSelector(), op);
  }

  public void close() {
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

  private void updateLastCommunication() {
    lastCommunication = getCurrentSecond();
  }

  public int doReadEvent() {
    if (state == ClientState.IDEL) {
      updateLastCommunication();
      try {
        int size = channel.read(buffer);
        if (size == -1) {
          close();
        } else if (size < MAX_BUFFER_SIZE) {
          buffer.flip();
          handleRequest();
          register(SelectionKey.OP_WRITE);
          state = ClientState.WRITING;
        } else {
          // violate the rule: less than 4k per message
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

  public int doWriteEvent() {
    if (state == ClientState.WRITING) {
      updateLastCommunication();
      try {
        int s = channel.write(out);
        if (!out.hasRemaining()) {
          out = null;
          state = ClientState.IDEL;
          register(SelectionKey.OP_READ);
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
    JRedisObject ans;
    try {
      var req = processCommand();
      state = ClientState.WAITING;
      var resp = server.processRequest(req, this);
      state = ClientState.WRITING;
      out = resp.getOut();
    } catch (Exception e) {
      ans = new JRString(e.getMessage());
      try {
        out = ByteBuffer.wrap(ans.serialize());
      } catch (JRedisTypeNotMatch ex) {
        // this will never happen
        out = ByteBuffer.wrap("JRedisTypeNotMatch error in handleRequest".getBytes());
      }
    }
  }

  private JRedisRequest processCommand()
      throws JRedisDataBaseException, JRedisTypeNotMatch, IOException {
    return acceptMsgAndBuildRequest();
  }

  private JRedisRequest acceptMsgAndBuildRequest() throws JRedisTypeNotMatch, IOException {
    int size = buffer.remaining();
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
    // log.info(
    //     "IP: {} port: {} command: {}, args: {}",
    //     ip.getHostAddress(),
    //     port,
    //     CommandContainer.getCommand(command).getName(),
    //    commandAgs);
    return new JRedisRequest(Command.getCommand(command), commandAgs, size);
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

  @Override
  public String toString() {
    return "JRedisClient{" + "ip=" + ip + ", port=" + port + '}';
  }

  public enum ClientState {
    IDEL,

    READING,

    WRITING,

    WAITING
  }
}
