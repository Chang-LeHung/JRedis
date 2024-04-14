package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public class AbstractCommand implements CommandAcceptor {

  private final Command command;

  public AbstractCommand(Command command) {
    this.command = command;
  }

  @Override
  public String getName() {
    return command.getName();
  }

  @Override
  public byte getFlag() {
    return command.getFlag();
  }

  @Override
  public JRedisObject accept(
      RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    throw new UnsupportedOperationException("not implemented");
  }
}
