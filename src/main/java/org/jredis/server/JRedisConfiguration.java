package org.jredis.server;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class JRedisConfiguration {

  private int port;

  private static final int defaultPort = 8080;

  private static final int defaultNumDB = 16;

  private static final double defaultLoadFactor = 0.75;

  private static final long defaultKeepAlive = 1000;

  private int numDB;

  private double loadFactor;

  private long keepAlive = 1000;

  public JRedisConfiguration() {
    this(defaultPort, defaultNumDB, defaultLoadFactor, defaultKeepAlive);
  }

  public JRedisConfiguration(Map<String, Object> config) {
    try{
      int port = defaultPort;
      if (config.containsKey("port")) {
        port = (int) config.get("port");
      }
      int numDB = defaultNumDB;
      if (config.containsKey("numDB")) {
        numDB = (int) config.get("numDB");
      }
      double loadFactor = defaultLoadFactor;
      if (config.containsKey("loadFactor")) {
        loadFactor = (double) config.get("loadFactor");
      }
      long keepAlive = defaultKeepAlive;
      if (config.containsKey("keepAlive")) {
        keepAlive = (long) config.get("keepAlive");
      }
      this.port = port;
      this.numDB = numDB;
      this.loadFactor = loadFactor;
      this.keepAlive = keepAlive;
    }catch (ClassCastException e) {
      log.error(e.getMessage());
      System.exit(1);
    }
  }

  public JRedisConfiguration(int port, int numDB, double loadFactor, long keepAlive) {
    this.port = port;
    this.numDB = numDB;
    this.loadFactor = loadFactor;
    this.keepAlive = keepAlive;
  }
}
