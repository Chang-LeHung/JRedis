package org.jredis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.server.middlewares.JRedisRequest;
import org.jredis.server.middlewares.JRedisResponse;
import org.jredis.server.middlewares.RequestProcessor;
import org.jredis.server.middlewares.ResponseProcessor;
import org.jredis.string.JRString;

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

  private final List<RequestProcessor> requestProcessors;

  private final List<ResponseProcessor> responseProcessors;

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
    requestProcessors = new LinkedList<>();
    responseProcessors = new LinkedList<>();
  }

  public void addClient(JRedisClient client) {
    log.info("Adding client: {}", client);
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
    log.info("Processing read event for client: {}", client);
    client.doReadEvent();
  }

  private void processWrite(SelectionKey key) {
    var ch = (SocketChannel)key.channel();
    JRedisClient client = getClient(ch);
    log.info("Processing write event for client: {}", client);
    client.doWriteEvent();
  }

  public void eventPoll() throws IOException {
    selector.select();
    dispatchEvent(selector.selectedKeys());
  }

  private void dispatchEvent(Set<SelectionKey> keys) throws IOException {
    Iterator<SelectionKey> iterator = keys.iterator();
    log.info("Dispatching {} events", keys.size());
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
    try {
      server.close();
      selector.close();
    } catch (IOException ignore) {
    }
    if (exitCode == 0) {
      log.info("JRedis exit");
    } else {
      log.error("JRedis exit, exit code = {}", exitCode);
    }
    System.exit(exitCode);
  }

  private JRedisResponse processCommand(JRedisRequest request, JRedisClient client) throws JRedisDataBaseException, JRedisTypeNotMatch, IOException {
    var command = request.getCommand();
    var args = request.getArgs();
    var ret = database.execute(command.getFlag(), args);
    return new JRedisResponse(ret, request);
  }

  public JRedisResponse processRequest(JRedisRequest request, JRedisClient client) {
    preProcessRequest(request, client);
    JRedisResponse res = null;
    try {
      res = processCommand(request, client);
    } catch (JRedisDataBaseException | JRedisTypeNotMatch | IOException  e) {
      // be careful with OOM
      try {
        res = new JRedisResponse(new JRString(e.getMessage()), request);
        res.setError(true);
      } catch (Exception ignored) {
      }
      log.error("JRedis process command error: {}", e.getMessage());
    }
    postProcessResponse(res, client);
    return res;
  }

  private void preProcessRequest(JRedisRequest request, JRedisClient client) {
    for (RequestProcessor processor : requestProcessors) {
      processor.processRequest(client, this, request);
    }
  }
  
  private void postProcessResponse(JRedisResponse response, JRedisClient client) {
    for (ResponseProcessor processor : responseProcessors) {
      processor.processResponse(client, this, response);
    }
  }

  public void addRequestProcessor(RequestProcessor processor) {
    requestProcessors.add(processor);
  }

  public void removeRequestProcessor(RequestProcessor processor) {
    requestProcessors.remove(processor);
  }

  public void addResponseProcessor(ResponseProcessor processor) {
    responseProcessors.add(processor);
  }

  public void removeResponseProcessor(ResponseProcessor processor) {
    responseProcessors.remove(processor);
  }
}
