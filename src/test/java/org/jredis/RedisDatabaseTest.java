package org.jredis;

import org.jredis.command.CommandReturnState;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.list.JRList;
import org.jredis.string.JRString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RedisDatabaseTest {

  private static final TestLogger logger = TestLogger.getLogger(RedisDatabaseTest.class);

  private static final RedisDatabase redis = new RedisDatabase( 0.75);

  @BeforeAll
  public static void testSetGet() {
    RedisDatabase.initRedisDatabase();
  }

  @Test
  public void testSetAndGet() throws JRedisDataBaseException, IOException {
    JRString name = new JRString("name");
    JRString val = new JRString("val");
    redis.set(name, val);
    assert redis.get(name).equals(val);
    assert redis.exists(name).equals(CommandReturnState.OK);
    logger.pass("get pass");
    logger.pass("set pass");
    logger.pass("exits pass");
  }

  @Test
  public void testMget() throws JRedisDataBaseException, IOException {
    var k1 = new JRString("k1");
    var k2 = new JRString("k2");
    var k3 = new JRString("k3");
    var k4 = new JRString("k4");

    var v1 = new JRString("v1");
    var v2 = new JRString("v2");
    var v3 = new JRString("v3");
    var v4 = new JRString("v4");

    redis.set(k1, v1);
    redis.set(k2, v2);
    redis.set(k3, v3);
    redis.set(k4, v4);

    assert redis.get(k1).equals(v1);
    assert redis.get(k2).equals(v2);
    assert redis.get(k3).equals(v3);
    assert redis.get(k4).equals(v4);

    assert redis.exists(k1).equals(CommandReturnState.OK);
    assert redis.exists(k2).equals(CommandReturnState.OK);
    assert redis.exists(k3).equals(CommandReturnState.OK);
    assert redis.exists(k4).equals(CommandReturnState.OK);

    JRList ans = new JRList();
    ans.appendRight(v1);
    ans.appendRight(v2);
    ans.appendRight(v3);
    ans.appendRight(v4);

    assert redis.mget(k1, k2, k3, k4).equals(ans);

    logger.pass("mget() pass");
  }
}
