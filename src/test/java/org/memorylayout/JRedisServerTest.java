package org.memorylayout;

import org.jredis.RedisDatabase;
import org.jredis.hash.JRIncrementalHash;
import org.jredis.server.JRedisConfiguration;
import org.jredis.server.JRedisServer;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import java.io.IOException;

public class JRedisServerTest {

  @Test
  public void testServer() throws IOException {
    JRedisServer server = new JRedisServer(new JRedisConfiguration());
    ClassLayout layout = ClassLayout.parseInstance(server);
    System.out.println(layout.toPrintable());

    layout = ClassLayout.parseClass(RedisDatabase.class);
    System.out.println(layout.toPrintable());

    layout = ClassLayout.parseClass(JRIncrementalHash.class);
    System.out.println(layout.toPrintable());
  }

  public static void main(String[] args) throws IOException {
    JRedisServer server = new JRedisServer(new JRedisConfiguration());
    ClassLayout layout = ClassLayout.parseInstance(server);
    System.out.println(layout.toPrintable());

    layout = ClassLayout.parseClass(RedisDatabase.class);
    System.out.println(layout.toPrintable());

    layout = ClassLayout.parseClass(JRIncrementalHash.class);
    System.out.println(layout.toPrintable());

    server.shutDown(0);
  }
}
