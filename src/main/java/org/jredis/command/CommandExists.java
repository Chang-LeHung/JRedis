package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public class CommandExists extends AbstractCommand {
  public static final CommandExists EXISTS = new CommandExists(Command.EXISTS);

  private CommandExists(Command command) {
    super(command);
  }

  @Override
  public JRedisObject accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length != 1) {
      throw new JRedisDataBaseException("Wrong number of arguments");
    }
    if (database.getDb().contains(args[0]))
      return CommandReturnState.OK;
    return CommandReturnState.Nil;
  }
}
