package org.jredis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.list.JRList;
import org.jredis.number.JRDouble;
import org.jredis.number.JRInt;
import org.jredis.string.JRString;

public class JRSerializerUtil {

  public static JRedisObject deserialize(byte[] data, int low, int high) throws JRedisTypeNotMatch, IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(data, low, high - low);
    return deserialize(stream);
  }

  public static JRedisObject deserialize(InputStream stream) throws JRedisTypeNotMatch, IOException {
    stream.mark(1);
    int idx = stream.read();
    stream.reset();
    JRedisObject o = null;
    switch (JRType.VALUES[idx]) {
      case LIST -> {
        o = new JRList();
        o.deserialize(stream);
      }
      case INT -> {
        o = new JRInt(0);
        o.deserialize(stream);
      }
      case DOUBLE -> {
        o = new JRDouble(0);
        o.deserialize(stream);
      }
      case STRING -> {
        o = new JRString();
        o.deserialize(stream);
      }
      case HASH -> {
        o = new JRIncrementalHash<>();
        o.deserialize(stream);
      }
    }
    return o;
  }
}
