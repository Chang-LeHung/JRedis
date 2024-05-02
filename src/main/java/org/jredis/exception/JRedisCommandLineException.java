package org.jredis.exception;

public class JRedisCommandLineException extends JRedisException {
    public JRedisCommandLineException() {
    }

    public JRedisCommandLineException(String message) {
        super(message);
    }
}
