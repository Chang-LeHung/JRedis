package org.jredis.command;

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

    DECRBY("decrby");


    private static byte cnt = 0;
    /**
     * the name of command
     */
    private final String name;
    /**
     * the flag of command
     */
    private final byte flag;

    private static final Map<Byte, Command> map = new HashMap<>();

    static {
        for (Command command : Command.values()) {
            map.put(command.getFlag(), command);
        }
    }

    Command(String name) {
      this.name = name;
      flag = getCnt();
    }

    private static byte getCnt() {
        return cnt++;
    }

    public String getName() {
        return name;
    }

    public byte getFlag() {
        return flag;
    }

    public static Command getCommand(byte flag) {
        return map.get(flag);
    }
}
