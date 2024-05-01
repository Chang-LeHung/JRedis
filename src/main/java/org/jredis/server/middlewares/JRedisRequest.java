package org.jredis.server.middlewares;

import lombok.Getter;
import org.jredis.JRedisObject;
import org.jredis.command.Command;

@Getter
public class JRedisRequest {

  private final int size;

  private final Command command;

  private final JRedisObject[] args;

  public JRedisRequest(Command command, JRedisObject[] args) {
    this.command = command;
    this.args = args;
    this.size = command.getFlag();
  }

  public int getArgSize() {
    return args.length;
  }
}
