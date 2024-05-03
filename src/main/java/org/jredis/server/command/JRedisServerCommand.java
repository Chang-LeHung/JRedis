package org.jredis.server.command;

import java.util.HashMap;
import java.util.Map;
import org.jredis.command.Command;

public class JRedisServerCommand {

  private static final Map<Byte, AbstractServerCommand> serverCommandMap = new HashMap<>();

  static {
    serverCommandMap.put(Command.INFO.getFlag(), CommandInfo.INFO);
    serverCommandMap.put(Command.SHUTDOWN.getFlag(), CommandShutdown.SHUTDOWN);
  }

  public static AbstractServerCommand getCommand(byte flag) {
    return serverCommandMap.get(flag);
  }

  public static AbstractServerCommand getCommand(Command command) {
    return serverCommandMap.get(command.getFlag());
  }

  public static boolean contains(byte flag) {
    return serverCommandMap.containsKey(flag);
  }
}
