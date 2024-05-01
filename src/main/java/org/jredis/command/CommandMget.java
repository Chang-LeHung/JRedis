package org.jredis.command;

import java.util.Objects;
import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.list.JRList;

public class CommandMget extends AbstractCommand {
  public static final CommandMget MGET = new CommandMget(Command.MGET);

  private CommandMget(Command command) {
    super(command);
  }

  @Override
  public JRedisObject accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length < 1)
      throw new JRedisDataBaseException("MGET requires at least one argument");
    JRList res = new JRList();
    for (JRedisObject arg : args) {
      JRedisObject obj = database.get(arg);
      res.appendRight(Objects.requireNonNullElse(obj, CommandReturnState.Nil));
    }
    return res;
  }
}
