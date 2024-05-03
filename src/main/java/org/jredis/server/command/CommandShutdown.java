package org.jredis.server.command;

import org.jredis.JRedisObject;
import org.jredis.command.Command;
import org.jredis.command.CommandReturnState;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.server.JRedisServer;

public class CommandShutdown extends AbstractServerCommand {
  private CommandShutdown(Command command) {
    super(command);
  }

  public static final CommandShutdown SHUTDOWN = new CommandShutdown(Command.SHUTDOWN);

  @Override
  public JRedisObject accept(JRedisServer server, JRedisObject... args) throws JRedisDataBaseException {
    server.close();
    return CommandReturnState.OK;
  }
}
