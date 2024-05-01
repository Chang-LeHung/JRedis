package org.jredis.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Command {
  SET("set"),

  GET("get"),

  DEL("del"),

  EXISTS("exists"),

  TYPE("type"),

  PERSIST("persist"),

  TTL("ttl"),

  EXPIRE("expire"),

  INCR("incr"),

  INCRBY("incrby"),

  DECR("decr"),

  MGET("mget"),

  DECRBY("decrby"),

  INFO("info");

  private static final Map<Byte, Command> map = new HashMap<>();
  private static byte cnt = 0;

  static {
    for (Command command : Command.values()) {
      map.put(command.getFlag(), command);
    }
  }

  /** the name of command */
  private final String name;
  /** the flag of command */
  private final byte flag;

  Command(String name) {
    this.name = name;
    flag = getCnt();
  }

  private static byte getCnt() {
    return cnt++;
  }

  public static Command getCommand(byte flag) {
    return map.get(flag);
  }

  public static void main(String[] args) {
    System.out.println(Arrays.toString(Command.values()));
  }

  public String getName() {
    return name;
  }

  public byte getFlag() {
    return flag;
  }

  @Override
  public String toString() {
    return "Command{" +
        "name='" + name + '\'' +
        ", flag=" + flag +
        '}';
  }
}
