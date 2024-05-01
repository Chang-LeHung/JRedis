package org.jredis.server.middlewares;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

@Getter
public class JRedisResponse {

  private final JRType type;
  private final int byteSize;
  private final JRedisObject retObj;
  private final JRedisRequest request;
  @Setter
  private ByteBuffer out;
  @Setter
  private boolean isError = false;

  public JRedisResponse(JRedisObject o, JRedisRequest request)
      throws JRedisTypeNotMatch, IOException {
    retObj = o;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    o.serialize(stream);
    var arr = stream.toByteArray();
    out = ByteBuffer.wrap(arr);
    byteSize = arr.length;
    this.type = o.getType();
    this.request = request;
  }

  public boolean hasRemaining() {
    return out.hasRemaining();
  }
}
