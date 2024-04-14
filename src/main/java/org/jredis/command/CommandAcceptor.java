package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public interface CommandAcceptor {

  String getName();

  byte getFlag();

  <K extends JRedisObject, V extends JRedisObject> JRedisObject  accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException;
}
