package org.jredis.exception;

public class JRedisTypeNotMatch extends JRedisException {
  public JRedisTypeNotMatch() {
  }

  public JRedisTypeNotMatch(String message) {
    super(message);
  }
}
