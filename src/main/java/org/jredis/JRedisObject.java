package org.jredis;

import java.io.*;
import org.jredis.exception.JRedisTypeNotMatch;

public class JRedisObject {

  public byte[] serialize() throws JRedisTypeNotMatch {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      serialize(stream);
    } catch (IOException ignore) {
    }
    return stream.toByteArray();
  }

  /**
   * @return size(bytes) written by this object
   */
  public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
    String name = this.getClass().getName();
    throw new UnsupportedOperationException("Class " + name + " is not serializable");
  }

  /**
   * @param data byte array
   * @param low include
   * @param high exclude
   * @return size(bytes) read by this object
   */
  public int deserialize(byte[] data, int low, int high) throws JRedisTypeNotMatch, IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(data, low, high - low);
    return deserialize(stream);
  }

  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    String name = this.getClass().getName();
    throw new UnsupportedOperationException("Class " + name + " is not serializable");
  }

  /**
   * @return 1. -1: can not be serialized 2. >0: real size of the object
   */
  public int serialSize() throws JRedisTypeNotMatch {
    String name = this.getClass().getName();
    throw new UnsupportedOperationException("Class " + name + " is not serializable");
  }
}
