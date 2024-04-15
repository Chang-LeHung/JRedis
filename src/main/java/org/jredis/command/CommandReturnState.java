package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.string.JRString;

public class CommandReturnState {

  public static final JRedisObject OK = new JRString("OK");

  public static final JRedisObject Nil = new JRString("Nil");
}
