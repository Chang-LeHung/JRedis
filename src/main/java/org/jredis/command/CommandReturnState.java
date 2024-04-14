package org.jredis.command;

import org.jredis.JRedisObject;
import org.jredis.string.JRString;

public class CommandReturnState {

  public static JRedisObject OK = new JRString("OK");

  public static JRedisObject Nil = new JRString("Nil");
}
