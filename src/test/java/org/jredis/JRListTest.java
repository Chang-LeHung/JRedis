package org.jredis;

import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.list.JRList;
import org.jredis.string.JRString;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JRListTest {

  public static TestLogger logger = new TestLogger(JRList.class);

  @Test
  public void testJRList() throws IOException, JRedisTypeNotMatch {
    JRList jrl = new JRList();
    jrl.appendRight(new JRString("hello"));
    jrl.appendRight(new JRString("world"));
    assert jrl.getSize() == 2;
    assert jrl.toString().equals("[JRString{hello}, JRString{world}]");
    logger.pass("toString() passed");
    logger.pass("getSize() passed");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    jrl.serialize(stream);
    JRList jrList = new JRList();
    ByteArrayInputStream s = new ByteArrayInputStream(stream.toByteArray());
    jrList.deserialize(s);
    assert jrl.equals(jrList);
    logger.pass("toString() passed");
    logger.pass("deserialize() passed");
  }
}
