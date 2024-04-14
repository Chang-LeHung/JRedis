package org.jredis;

import org.jredis.command.CommandReturnState;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.string.JRString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RedisDatabaseTest {

  private static final TestLogger logger = TestLogger.getLogger(RedisDatabaseTest.class);

  private static final RedisDatabase redis = new RedisDatabase( 0.75);

  @BeforeAll
  public static void testSetGet() {
    RedisDatabase.initRedis();
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
}
