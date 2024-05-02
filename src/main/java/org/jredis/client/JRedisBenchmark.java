package org.jredis.client;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;

public class JRedisBenchmark {

  private static final int OP_COUNT = 10000;

  private final int clientCount;

  private final List<JRedisClient> clients;

  private final JRedisProfiler profiler;

  private final int opCount;

  private static class ProfilerRecord {
    @Getter
    @Setter
    private long startTime;

    @Getter
    @Setter
    private long endTime;

    @Getter
    private final int opCount;

    @Getter
    private final String name;

    public ProfilerRecord(int opCount) {
      name = "default";
      this.opCount = opCount;
    }

    public ProfilerRecord(String name, int opCount) {
      this.name = name;
      this.opCount = opCount;
    }

    public ProfilerRecord(String name, long startTime, long endTime, int opCount) {
      this.name = name;
      this.startTime = startTime;
      this.endTime = endTime;
      this.opCount = opCount;
    }

    public double getQPS() {
      return (double) opCount / (endTime - startTime) * 1000;
    }

    @Override
    public String toString() {
      return "ProfilerRecord{" +
          "name='" + name + '\'' +
          "QPS=" + getQPS() +
          '}';
    }
  }

  private static class JRedisProfiler {


    @Getter
    private List<ProfilerRecord> records;

    @Getter
    private ProfilerRecord activeRecord;

    @Getter
    private final int opCount;

    public JRedisProfiler(int opCount) {
      this.opCount = opCount;
      activeRecord = new ProfilerRecord(opCount);
      records = new CopyOnWriteArrayList<>();
    }

    public JRedisProfiler(int opCount, String firstRecordName) {
      this.opCount = opCount;
      activeRecord = new ProfilerRecord(firstRecordName, opCount);
    }

    void startProfiling() {
      activeRecord.setStartTime(System.currentTimeMillis());
    }

    void endProfiling() {
      activeRecord.setEndTime(System.currentTimeMillis());
    }

    public double getQPS() {
      return activeRecord.getQPS();
    }

    public void newRecord(String name) {
      records.add(activeRecord);
      activeRecord = new ProfilerRecord(name, opCount);
    }

    public void replaceActiveRecord(String name) {
      activeRecord = new ProfilerRecord(name, opCount);
    }
  }

  public JRedisBenchmark(int opCount, int threadCount, String ip, int port) throws IOException {
    clientCount = threadCount;
    clients = new CopyOnWriteArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      clients.add(new JRedisClient(ip, port));
    }
    profiler = new JRedisProfiler(opCount * threadCount);
    this.opCount = opCount;
  }

  private void testGET(int tidx) {
    for(int i = 0; i < opCount; i++) {
      try {
        clients.get(tidx).execute("get " + UUID.randomUUID());
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }


  @FunctionalInterface
  private interface JRedisAction {
    void accept(int idx);
  }

  private void doBenchmark(JRedisAction action) {
    var threads = new CopyOnWriteArrayList<Thread>();
    for (int i = 0; i < clientCount; i++) {
      final int idx = i;
      var thread = new Thread(() -> {
        action.accept(idx);
      });
      threads.add(thread);
      thread.start();
    }
    threads.forEach((t) -> {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }
    });
  }

  private ProfilerRecord benchmarkGET() {
    profiler.newRecord("Get");
    profiler.startProfiling();
    doBenchmark(this::testGET);
    profiler.endProfiling();
    return profiler.getActiveRecord();
  }

  private void testSet(int idx) {
    String command = null;
    for(int i = 0; i < opCount; i++) {
      try{
        command = "set " + UUID.randomUUID() + " " + UUID.randomUUID();
        clients.get(idx).execute(command);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    System.out.println(command);
  }

  private ProfilerRecord benchmarkSET() {
    profiler.newRecord("Set");
    profiler.startProfiling();
    doBenchmark(this::testSet);
    profiler.endProfiling();
    return profiler.getActiveRecord();
  }

  public void boot() {
    var record = benchmarkSET();
    System.out.println(record);
    record = benchmarkGET();
    System.out.println(record);
  }

  public static void main(String[] args) throws IOException {
    var benchmark = new JRedisBenchmark(10000, 50, "127.0.0.1", 8086);
    benchmark.boot();
  }
}
