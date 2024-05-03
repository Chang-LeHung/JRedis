package org.jredis;

import org.jredis.command.*;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.number.JRInt;
import org.jredis.string.JRString;

import java.io.*;

public class RedisDatabase extends JRedisObject {

  private static final String filename = "db.jrd";

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
    return execute(Command.EXISTS.getFlag(), key);
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

  @Override
  public JRType getType() {
    return JRType.DATABASE;
  }

  @Override
  public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
    return db.serialize(out) + expire.serialize(out);
  }


  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    return db.deserialize(stream) + expire.deserialize(stream);
  }

  @Override
  public int serialSize() throws JRedisTypeNotMatch {
    return db.serialSize() + expire.serialSize();
  }

  public void fsync() throws JRedisTypeNotMatch, IOException {
    fsync(filename);
  }

  public void fsync(String filename) throws IOException, JRedisTypeNotMatch {
    FileOutputStream fs = new FileOutputStream(filename);
    BufferedOutputStream stream = new BufferedOutputStream(fs);
    db.serialize(stream);
    expire.serialize(stream);
    stream.flush();
    fs.getFD().sync();
  }

  public void load(String filename) throws JRedisTypeNotMatch, IOException {
    FileInputStream fs;
    try {
      fs = new FileInputStream(filename);
    } catch (FileNotFoundException ignore) {
      return;
    }
    BufferedInputStream stream = new BufferedInputStream(fs);
    db.deserialize(stream);
    expire.deserialize(stream);
  }

  public void load() throws JRedisTypeNotMatch, IOException {
    load(filename);
  }
}
