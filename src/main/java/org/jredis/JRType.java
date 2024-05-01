package org.jredis;

import java.util.Arrays;

public enum JRType {
  STRING,
  LIST,
  SET,
  ZSET,
  HASH,
  INT,
  DOUBLE;

  public static final JRType[] VALUES = values();

  public static byte cnt;

  public final byte FLAG_NUMBER;

  JRType() {
    FLAG_NUMBER = getCnt();
  }

  private static byte getCnt() {
    return cnt++;
  }

  public static void main(String[] args){
    System.out.println(Arrays.toString(VALUES));
  }

  @Override
  public String toString() {
    return name() +  "{" +
        "FLAG_NUMBER=" + FLAG_NUMBER +
        '}';
  }
}
