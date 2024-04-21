package org.jredis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jredis.RedisDatabase;

@Slf4j
public class JRedisServer {

  private final Map<Channel, JRedisClient> clients;

  @Getter
  private final Selector selector;

  private final ServerSocketChannel server;

  @Getter
  private final long keepAlive;

  private final RedisDatabase database;

  private ServerState state;

  private enum ServerState {

    INIT,

    RUNNING,

    STOPPED
  }

  public JRedisServer(JRedisConfiguration config) throws IOException {
    clients = new HashMap<>();
    selector = Selector.open();
    server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress(config.getPort()));
    server.configureBlocking(false);
    server.register(selector, SelectionKey.OP_ACCEPT, this);
    keepAlive = config.getKeepAlive();
    database = new RedisDatabase(config.getLoadFactor());
    state = ServerState.RUNNING;
  }

  public void addClient(JRedisClient client) {
    clients.put(client.getChannel(), client);
  }

  public JRedisClient getClient(Channel channel) {
    return clients.get(channel);
  }

  public void removeClient(JRedisClient client) {
    var ch = client.getChannel();
    clients.remove(ch);
  }

  private void processAccept() throws IOException {
    SocketChannel sc = server.accept();
    assert sc != null;
    sc.configureBlocking(false);
    JRedisClient client = new JRedisClient(sc, this);
    client.register(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    addClient(client);
  }

  private void processRead(SelectionKey key) {
    var ch = (SocketChannel) key.channel();
    JRedisClient client = getClient(ch);
    client.read();
  }

  private void processWrite(SelectionKey key) {
    var ch = (SocketChannel)key.channel();
    JRedisClient client = getClient(ch);
    client.write();
  }

  public void eventPoll() throws IOException {
    int size = selector.select();
    if (size == 0) return;
    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
    while (iterator.hasNext()) {
      SelectionKey key = iterator.next();
      if (key.isValid() && key.isAcceptable()) {
        processAccept();
      }
      if (key.isValid() && key.isReadable()) {
        processRead(key);
      }
      // key.isValid() is required, the key may be removed in processRead
      if (key.isValid() && key.isWritable()) {
        processWrite(key);
      }
      iterator.remove();
    }
  }

  private void serverCron() {

  }

  public RedisDatabase getActiveDatabase() {
    return database;
  }

  public void eventLoop() {
    log.info("JRedis boot");
    while (state != ServerState.STOPPED) {
      try {
        eventPoll();
      } catch (IOException e) {
        log.error("Server crashed: {}", e.getMessage());
        shutDown(1);
      }
      serverCron();
    }
  }

  public void shutDown(int exitCode) {
    state = ServerState.STOPPED;
    if (exitCode == 0) {
      log.info("JRedis exit");
    } else {
      log.error("JRedis exit, exit code = {}", exitCode);
    }
    System.exit(exitCode);
  }
}
