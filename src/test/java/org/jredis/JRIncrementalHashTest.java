package org.jredis;

import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.string.JRString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JRIncrementalHashTest {

  public static TestLogger logger = new TestLogger(JRIncrementalHash.class);

  @Test
  public void testHash() throws IOException, JRedisTypeNotMatch {
    JRIncrementalHash<JRedisObject, JRedisObject> hash = new JRIncrementalHash<>(2, 0.5);
    var key = new JRString("key");
    var val = new JRString("value");
    hash.put(key, val);
    assert hash.get(key).equals(val);
    assert hash.put(key, val).equals(val);
    var key1 = new JRString("key1");
    var val1 = new JRString("value1");
    var key2 = new JRString("key2");
    var val2 = new JRString("value2");
    hash.put(key1, val1);
    hash.put(key2, val2);
    logger.pass("put() passed");
    logger.pass("get() passed");
    Assertions.assertEquals(3, hash.getSize());
    logger.pass("getSize() passed");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    hash.serialize(stream);
    ByteArrayInputStream s = new ByteArrayInputStream(stream.toByteArray());
    JRIncrementalHash<JRedisObject, JRedisObject> copy = new JRIncrementalHash<>(0.5);
    copy.deserialize(s);
    Assertions.assertEquals(hash, copy);
    logger.pass("serialize() passed");
    logger.pass("deserialize() passed");
  }
}
