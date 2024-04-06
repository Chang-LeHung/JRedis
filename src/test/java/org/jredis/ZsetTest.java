package org.jredis;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.number.JRInt;
import org.jredis.string.JRString;
import org.jredis.zset.SkipList;
import org.junit.jupiter.api.Test;

public class ZsetTest {

  public static TestLogger logger = new TestLogger(SkipList.class);

  @Test
  public void testZset() throws IOException {
    SkipList<String, String> zset = new SkipList<String, String>();
    zset.put("a", "1");
    zset.put("b", "2");
    zset.put("c", "3");
    zset.put("d", "4");
    zset.put("e", "5");
    zset.put("f", "6");
    zset.put("g", "7");
    zset.put("h", "8");
    zset.put("i", "9");
    logger.pass(zset);
    assert zset.getSize() == 9;
    logger.pass("getSize() passed");
    logger.pass("put() passed");

    zset.remove("e");
    zset.remove("f");
    zset.remove("g");
    zset.remove("h");
    zset.remove("i");
    logger.pass(zset);
    assert zset.getSize() == 4;
    assert zset.contains("a");
    assert zset.contains("b");
    assert zset.contains("c");
    assert zset.contains("d");
    assert !zset.contains("e");
    assert !zset.contains("f");
    assert !zset.contains("g");
    assert !zset.contains("h");
    assert !zset.contains("i");
    assert zset.get("a").equals("1");
    assert zset.get("b").equals("2");
    assert zset.get("c").equals("3");
    assert zset.get("d").equals("4");
    logger.pass("contains() passed");
    logger.pass("remove() passed");
    logger.pass("get() passed");
    String ans = zset.put("a", "1");
    assert ans.equals("1");
    logger.pass(zset);
    List<String> range = zset.getRange("b", "c");
    logger.pass(range);
    range = zset.getRange("", "c");
    logger.pass(range);
  }

  @Test
  public void testSerialization() throws JRedisTypeNotMatch, IOException {
    SkipList<JRInt, JRString> zset = new SkipList<>();
    zset.put(new JRInt(1), new JRString("1"));
    zset.put(new JRInt(2), new JRString("2"));
    zset.put(new JRInt(3), new JRString("3"));
    zset.put(new JRInt(4), new JRString("4"));
    zset.put(new JRInt(5), new JRString("5"));
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    zset.serialize(stream);

    SkipList<JRInt, JRString> copy = new SkipList<>();
    copy.deserialize(new ByteArrayInputStream(stream.toByteArray()));
    assert copy.equals(zset);
    logger.pass("equals() passed");
    assert stream.size() == zset.serialSize();
    logger.pass("serialSize() passed");
    copy.update(new JRInt(1), new JRString("2"));
    zset.get(new JRInt(1)).equals(new JRString("2"));
    logger.pass("update() passed");
    logger.pass(copy);
  }
}
