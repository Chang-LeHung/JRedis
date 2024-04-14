package org.jredis.command;

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

    DECR("decr");

    private static byte cnt = 0;
    /**
     * the name of command
     */
    private final String name;
    /**
     * the flag of command
     */
    private final byte flag;

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
}
