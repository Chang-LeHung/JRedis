package org.jredis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.number.JRInt;
import org.junit.jupiter.api.Test;

public class JRIntTest {

  public static TestLogger logger = TestLogger.getLogger(JRIntTest.class);

  @Test
  public void testJRInt() throws IOException {
    JRInt i = new JRInt(0);
    i.increment(1);
    assert i.getVal() == 1;
    i.decrement(1);
    assert i.getVal() == 0;
    logger.pass("increment()");
    logger.pass("decrement()");
    logger.pass("getVal()");
    i.increment(1);
    i.multiply(2);
    assert i.getVal() == 2;
    i.divide(2);
    assert i.getVal() == 1;
    i.mod(2);
    assert i.getVal() == 1;
    logger.pass("multiply()");
    logger.pass("divide()");
    logger.pass("mod()");

    assert !i.equals("1");
    assert i.equals(new JRInt(1));

    i.setVal(100);
    assert i.getVal() == 100;
    logger.pass("setVal()");
    logger.pass(i.hashCode());
    logger.pass(i.valueToString());
    logger.equals(null);

    ByteArrayInputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
    try {
      i.deserialize(stream);
    } catch (JRedisTypeNotMatch e) {
      logger.pass("deserialize()");
    }
  }
}
