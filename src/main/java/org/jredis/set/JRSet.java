package org.jredis.set;

import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JRSet<K extends JRedisObject> extends JRedisObject {

  @Override
  public int serialize(OutputStream out) throws IOException {
    return super.serialize(out);
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    return super.deserialize(stream);
  }

  @Override
  public int serialSize() throws JRedisTypeNotMatch {
    return super.serialSize();
  }
}

