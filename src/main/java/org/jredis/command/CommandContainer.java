package org.jredis.command;

import java.util.HashMap;
import java.util.Map;

public class CommandContainer {

  private static final Map<Byte, CommandAcceptor> byteCommands = new HashMap<>();

  private static final Map<String, CommandAcceptor> nameCommands = new HashMap<>();

  public static void addCommand(CommandAcceptor command) {
    byteCommands.put(command.getFlag(), command);
    nameCommands.put(command.getName().toLowerCase(), command);
  }

  public static CommandAcceptor getCommand(byte flag) {
    return byteCommands.get(flag);
  }

  public static CommandAcceptor getCommand(String name) {
    return nameCommands.get(name.toLowerCase());
  }

  public static void removeCommand(CommandAcceptor command) {
    byteCommands.remove(command.getFlag());
    nameCommands.remove(command.getName().toLowerCase());
  }

  public static void removeCommand(byte flag) {
    CommandAcceptor commandAcceptor = byteCommands.get(flag);
    if (commandAcceptor != null) {
      removeCommand(commandAcceptor);
    }
  }
}
