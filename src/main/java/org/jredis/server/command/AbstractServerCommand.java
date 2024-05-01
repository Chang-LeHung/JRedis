package org.jredis.server.command;

import org.jredis.JRedisObject;
import org.jredis.command.AbstractCommand;
import org.jredis.command.Command;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.server.JRedisServer;

public abstract class AbstractServerCommand extends AbstractCommand {


  public AbstractServerCommand(Command command) {
    super(command);
  }

  public abstract JRedisObject accept(JRedisServer server, JRedisObject... args)
      throws JRedisDataBaseException;
}
