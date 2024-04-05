package org.jredis.exception;

public class JRedisOutOfBound extends JRedisException {
  public JRedisOutOfBound() {}

  public JRedisOutOfBound(String message) {
    super(message);
  }
}
