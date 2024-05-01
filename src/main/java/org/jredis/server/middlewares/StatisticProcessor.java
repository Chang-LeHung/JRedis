package org.jredis.server.middlewares;

import lombok.Getter;
import org.jredis.server.JRedisClient;
import org.jredis.server.JRedisServer;

@Getter
public class StatisticProcessor implements RequestProcessor, ResponseProcessor {

  private int totalRequest;

  private long downloadSize;

  private long uploadSize;

  private int totalResponse;

  public StatisticProcessor() {
    totalRequest = 0;
    downloadSize = 0;
    uploadSize = 0;
    totalResponse = 0;
  }

  @Override
  public void processRequest(JRedisClient client, JRedisServer server, JRedisRequest request) {
    totalRequest += 1;
    uploadSize += request.getByteSize();
  }

  @Override
  public void processResponse(JRedisClient client, JRedisServer server, JRedisResponse response) {
    totalResponse += 1;
    downloadSize += response.getByteSize();
  }

}
