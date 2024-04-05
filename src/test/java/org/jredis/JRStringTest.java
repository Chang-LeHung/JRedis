package org.jredis;

import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.string.JRString;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JRStringTest {

  public static TestLogger logger = TestLogger.getLogger(JRString.class);

  @Test
  public void testJRString() throws IOException, JRedisTypeNotMatch {
    String s = "Hello World";
    JRString jrs = new JRString(s);
    assert jrs.getSize() == s.length();
    logger.pass("getSize() passed");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    jrs.serialize(stream);
    JRString copy = new JRString();
    copy.deserialize(new ByteArrayInputStream(stream.toByteArray()));
    assert copy.equals(jrs);
    logger.pass("serialize() passed");
    logger.pass("deserialize() passed");
  }
}

