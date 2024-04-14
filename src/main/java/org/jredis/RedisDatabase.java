package org.jredis;

import java.util.HashMap;
import java.util.Map;
import org.jredis.command.*;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.number.JRInt;
import org.jredis.string.JRString;

public class RedisDatabase {

  private final JRIncrementalHash<JRedisObject, JRedisObject> db;

  private final JRIncrementalHash<JRedisObject, JRInt> expire;

  public static final Map<Byte, CommandAcceptor> byteCommands = new HashMap<>();

  public static final Map<String, CommandAcceptor> nameCommands = new HashMap<>();

  public RedisDatabase(double loadFactor) {
    db = new JRIncrementalHash<>(loadFactor);
    expire = new JRIncrementalHash<>(loadFactor);
  }

  public static void initRedis() {
    byteCommands.put(Command.GET.getFlag(), CommandGet.GET);
    byteCommands.put(Command.SET.getFlag(), CommandSet.SET);
    byteCommands.put(Command.EXISTS.getFlag(), CommandExists.EXISTS);

    nameCommands.put(Command.GET.getName(), CommandGet.GET);
    nameCommands.put(Command.SET.getName(), CommandSet.SET);
  }

  public static void addByteCommand(byte flag, CommandAcceptor command) {
    byteCommands.put(flag, command);
  }

  public static void addNameCommand(String name, CommandAcceptor command) {
    nameCommands.put(name.toLowerCase(), command);
  }

  public JRedisObject get(JRedisObject key) throws JRedisDataBaseException {
    return execute(Command.GET.getFlag(), key);
  }

  public JRedisObject set(JRedisObject key, JRedisObject val) throws JRedisDataBaseException {
    return execute(Command.SET.getFlag(), key, val);
  }

  public JRedisObject del(JRedisObject key) throws JRedisDataBaseException {
    return execute(Command.DEL.getFlag(), key);
  }

  public JRedisObject exists(JRedisObject key) throws JRedisDataBaseException {
    return execute(Command.EXISTS.getFlag(), new JRedisObject[] {key});
  }

  public int getSize() {
    return db.getSize();
  }


  JRedisObject execute(String name, JRedisObject ...args) throws JRedisDataBaseException {
    CommandAcceptor commandAcceptor = nameCommands.get(name);
    if (commandAcceptor == null)
      throw new JRedisDataBaseException("Command not found");
    return commandAcceptor.accept(this, args);
  }


  JRedisObject execute(JRString string, JRedisObject ...args) throws JRedisDataBaseException {
    return execute(string.bufToString(), args);
  }


  public JRedisObject execute(byte flag, JRedisObject ...args) throws JRedisDataBaseException {
    CommandAcceptor command = byteCommands.get(flag);
    if (command == null)
      throw new JRedisDataBaseException("Command not found");
    return command.accept(this, args);
  }

  public JRIncrementalHash<JRedisObject, JRedisObject> getDb() {
    return db;
  }

  public JRIncrementalHash<JRedisObject, JRInt> getExpire() {
    return expire;
  }
}
