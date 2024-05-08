package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public class CommandPing extends AbstractCommand {
  private CommandPing(Command command) {
    super(command);
  }

  public static final CommandPing PING = new CommandPing(Command.PING);

  @Override
  public JRedisObject accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length != 0) {
      throw new JRedisDataBaseException("PING command should not have any arguments");
    }
    return CommandReturnState.PONG;
  }
}
