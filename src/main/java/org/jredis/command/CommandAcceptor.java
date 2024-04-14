package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.RedisDatabase;
import org.jredis.exception.JRedisDataBaseException;

public interface CommandAcceptor {

  String getName();

  byte getFlag();

  JRedisObject  accept(RedisDatabase database, JRedisObject... args) throws JRedisDataBaseException;
}
