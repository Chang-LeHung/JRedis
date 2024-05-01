package org.jredis.server.middlewares;

import java.util.Arrays;
import lombok.Getter;
import org.jredis.JRedisObject;
import org.jredis.command.Command;

@Getter
public class JRedisRequest {

  private final int size;

  private final Command command;

  private final JRedisObject[] args;

  private final int byteSize;

  public JRedisRequest(Command command, JRedisObject[] args, int byteSize) {
    this.command = command;
    this.args = args;
    this.size = command.getFlag();
    this.byteSize = byteSize;
  }

  public int getArgSize() {
    return args.length;
  }

  @Override
  public String toString() {
    return "JRedisRequest{" +
        "command=" + command +
        ", args=" + Arrays.toString(args) +
        '}';
  }
}
