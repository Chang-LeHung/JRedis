package org.jredis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jredis.server.JRedisConfiguration;
import org.jredis.server.JRedisServer;
import org.yaml.snakeyaml.Yaml;

@Slf4j
public class JRedisEntry {

  private static final String config = "properties.yml";

  private static JRedisConfiguration loadConfiguration() {
    Yaml yaml = new Yaml();
    InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
    Map<String, Object> config = yaml.load(inputStream);
    log.info("Loaded configuration: {}", config);
    return new JRedisConfiguration(config);
  }

  public static void main(String[] args) {
    var config = loadConfiguration();
    RedisDatabase.initRedisDatabase();
    JRedisServer server = null;
    try {
      server = new JRedisServer(config);
    } catch (IOException e) {
      log.error("Error starting server", e);
      System.exit(1);
    }
    log.info("JRedis server started, enter event loop");
    server.eventLoop();
  }
}
