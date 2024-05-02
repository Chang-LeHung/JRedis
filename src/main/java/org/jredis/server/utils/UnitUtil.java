package org.jredis.server.utils;

public class UnitUtil {
  
  private static final String b = "B";
  
  private static final long bLimit = 1L << 10;
  
  private static final String kb = "KB";
  
  private static final long kbLimit = 1L << 20;
  
  private static final String mb = "MB";
  
  private static final long mbLimit = 1L << 30;
  
  private static final String gb = "GB";
  
  private static final long gbLimit = 1L << 40;
  
  private static final String tb = "TB";
  
  private static final long tbLimit = 1L << 50;
  
  public static String format(long size) {
    if (size < bLimit) {
      return size + b;
    } else if (size < kbLimit) {
      return String.format("%.3f", (double)size / bLimit) + kb;
    } else if (size < mbLimit) {
      return String.format("%.3f", (double)size / kbLimit) + mb;
    } else if (size < gbLimit) {
      return String.format("%.3f", (double)size / mbLimit) + gb;
    } else if (size < tbLimit) {
      return String.format("%.3f", (double)size / gbLimit) + tb;
    } else {
      return size + b;
    }
  }

}
