package org.jredis.server.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jredis.JRedisObject;
import org.jredis.command.Command;
import org.jredis.exception.JRedisDataBaseException;
import org.jredis.server.JRedisServer;
import org.jredis.server.utils.UnitUtil;
import org.jredis.string.JRString;

public class CommandInfo extends AbstractServerCommand {
  public static final CommandInfo INFO = new CommandInfo(Command.INFO);
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private CommandInfo(Command command) {
    super(command);
  }

  @Override
  public JRedisObject accept(JRedisServer server, JRedisObject... args) throws JRedisDataBaseException {
    if (args.length != 0)
      throw new JRedisDataBaseException("INFO command takes no arguments");
    LocalDateTime startTime = server.getStartTime();
    var time = startTime.format(formatter);
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Server boot time: %s", time)).append("\n");
    sb.append(String.format("Server objects: %d\n", server.getActiveDatabase().getSize()));
    sb.append(String.format("Server clients: %d\n", server.getClientSize()));
    var statisticProcessor = server.getStatisticProcessor();
    sb.append(String.format("Total download size: %s\n", UnitUtil.format(statisticProcessor.getDownloadSize())));
    sb.append(String.format("Total upload size: %s\n", UnitUtil.format(statisticProcessor.getUploadSize())));
    sb.append(String.format("Total request: %d\n", statisticProcessor.getTotalRequest()));
    sb.append(String.format("Total response: %d", statisticProcessor.getTotalResponse()));
    return new JRString(sb.toString());
  }
}
