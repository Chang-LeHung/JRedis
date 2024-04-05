package org.jredis.exception;

public class JRedisException extends Exception {

  public JRedisException() {
  }

  public JRedisException(String message) {
    super(message);
  }
}
