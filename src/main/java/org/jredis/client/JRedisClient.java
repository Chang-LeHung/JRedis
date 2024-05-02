package org.jredis.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lombok.Getter;
import org.jredis.JRSerializerUtil;
import org.jredis.JRedisObject;
import org.jredis.command.Command;
import org.jredis.exception.JRedisCommandLineException;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.string.JRString;

public class JRedisClient {

  private static final int MAX_BUF_SIZE = 1 << 12; // 4 KB

  @Getter
  private final String ip;

  @Getter
  private final int port;

  private final SocketChannel channel;

  private final ByteBuffer buffer;

  public JRedisClient(String ip, int port) throws IOException {
    this.ip = ip;
    this.port = port;
    channel = SocketChannel.open(new InetSocketAddress(ip, port));
    buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
  }

  public JRedisObject execute(String command) throws JRedisCommandLineException, JRedisTypeNotMatch, IOException {
    String[] commandLine = command.strip().split("\\s+");
    if (commandLine.length == 0)
      throw new JRedisCommandLineException("please input a command");
    String commandName = commandLine[0].toLowerCase();
    Command cmd = Command.getCommand(commandName);
    if (cmd == null) {
      throw new JRedisCommandLineException("not found command \"" + commandName + "\"");
    }
    JRString[] args = new JRString[commandLine.length - 1];
    for (int i = 1; i < commandLine.length; i++) {
      args[i - 1] = new JRString(commandLine[i]);
    }
    return execute(cmd, args);
  }

  public JRedisObject execute(Command command, JRedisObject... args) throws JRedisTypeNotMatch, IOException {
    buffer.put(command.getFlag());
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    for (JRedisObject arg : args) {
      arg.serialize(stream);
    }
    buffer.put(stream.toByteArray());
    return launchRequest();
  }

  private JRedisObject launchRequest() throws IOException {
    buffer.flip();
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
    buffer.clear();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    JRedisObject ans = null;
    while (ans == null) {
      channel.read(buffer);
      buffer.flip();
      while (buffer.hasRemaining()) {
        stream.write(buffer.get());
      }
      buffer.clear();
      // try to get an object
      try{
        var inputStream = new ByteArrayInputStream(stream.toByteArray());
        ans = tryParse(inputStream);
      } catch (Exception ignore) {
      }
    }
    return ans;
  }

  public JRedisObject tryParse(InputStream stream) throws JRedisTypeNotMatch, IOException {
    return JRSerializerUtil.deserialize(stream);
  }

  public static void main(String[] args) throws IOException, JRedisCommandLineException, JRedisTypeNotMatch {
    JRedisClient client = new JRedisClient("127.0.0.1", 8086);
    var ans = client.execute("get name");
    System.out.println(ans);
  }

}
