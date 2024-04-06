package org.jredis;


import org.jredis.zset.SkipList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

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
}
