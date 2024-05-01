package org.jredis.server.middlewares;

import org.jredis.server.JRedisClient;
import org.jredis.server.JRedisServer;

public interface ResponseProcessor {

  void processResponse(JRedisClient client, JRedisServer server, JRedisResponse response);
}
