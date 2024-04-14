package org.jredis.command;


import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public class CommandSet extends AbstractCommand {

  public static final CommandSet SET = new CommandSet(Command.SET);

  private CommandSet(Command command) {
    super(command);
  }

  @Override
  public <K extends JRedisObject, V extends JRedisObject> JRedisObject accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length != 2) {
      throw new JRedisDataBaseException("the command \"set\" accepts and only accepts 2 arguments");
    }

    var key = (K) args[0];
    var val = (V) args[1];
    var ret = database.getDb().put(key, val);
    if (ret == null)
      return CommandReturnState.OK;
    return ret;
  }
}
