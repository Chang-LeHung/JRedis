package org.jredis;

import java.util.Arrays;
import org.apache.commons.io.EndianUtils;
import org.junit.jupiter.api.Test;

public class EndianTest {

  @Test
  public void testEndian() {
    int data = 100;
    byte[] bytes = new byte[4];
    EndianUtils.writeSwappedInteger(bytes, 0, data);
    System.out.println(Arrays.toString(bytes));
    int res = EndianUtils.readSwappedInteger(bytes, 0);
    System.out.println(res);
  }
}
