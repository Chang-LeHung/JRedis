package org.jredis;

import org.jredis.command.*;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.number.JRInt;
import org.jredis.string.JRString;

public class RedisDatabase {

  private final JRIncrementalHash<JRedisObject, JRedisObject> db;

  private final JRIncrementalHash<JRedisObject, JRInt> expire;

  public RedisDatabase(double loadFactor) {
    db = new JRIncrementalHash<>(loadFactor);
    expire = new JRIncrementalHash<>(loadFactor);
  }

  public static void initRedisDatabase() {
    CommandContainer.initRedisCommands();
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

  public JRedisObject mget(JRedisObject ...args) throws JRedisDataBaseException {
    return execute(Command.MGET.getFlag(), args);
  }

  public int getSize() {
    return db.getSize();
  }


  JRedisObject execute(String name, JRedisObject ...args) throws JRedisDataBaseException {
    CommandAcceptor commandAcceptor = CommandContainer.getCommand(name);
    if (commandAcceptor == null)
      throw new JRedisDataBaseException("Command not found");
    return commandAcceptor.accept(this, args);
  }


  JRedisObject execute(JRString string, JRedisObject ...args) throws JRedisDataBaseException {
    return execute(string.bufToString(), args);
  }


  public JRedisObject execute(byte flag, JRedisObject ...args) throws JRedisDataBaseException {
    CommandAcceptor command = CommandContainer.getCommand(flag);
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
