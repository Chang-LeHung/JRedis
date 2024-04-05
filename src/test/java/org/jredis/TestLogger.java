package org.jredis;

import java.io.IOException;
import java.io.OutputStream;

public class TestLogger {

  private final OutputStream out;
  private final String className;

  public static TestLogger getLogger(Class<?> clazz) {
    return new TestLogger(clazz);
  }

  public TestLogger(Class<?> clazz) {
    this(System.out, clazz);
  }

  public TestLogger(OutputStream stream, Class<?> clazz) {
    out = stream;
    className = clazz.getName();
  }

  public void pass(String info) throws IOException {
    String s = className + ":" + "\u001B[32m" + info + "\u001B[0m\n";
    out.write(s.getBytes());
    out.flush();
  }

  public void fail(String info) throws IOException {
    String s = className + ":" + "\u001B[31m" + info + "\u001B[0m\n";
    out.write(s.getBytes());
    out.flush();
  }
}
