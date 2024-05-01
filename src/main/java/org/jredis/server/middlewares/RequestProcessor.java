package org.jredis.server.middlewares;

import org.jredis.server.JRedisClient;
import org.jredis.server.JRedisServer;

public interface RequestProcessor {

  void processRequest(JRedisClient client, JRedisServer server, JRedisRequest request);
}
