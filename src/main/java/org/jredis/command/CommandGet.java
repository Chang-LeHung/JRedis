package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public class CommandGet extends AbstractCommand {
  public static final CommandGet GET = new CommandGet(Command.GET);

  private CommandGet(Command command) {
    super(command);
  }

  @Override
  public JRedisObject accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length != 1) {
      throw new JRedisDataBaseException("Wrong number of arguments");
    }
    var ret = database.getDb().get(args[0]);
    if (ret == null) return CommandReturnState.Nil;
    return ret;
  }
}
