package org.jredis.number;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.EndianUtils;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

public class JRInt extends JRedisObject implements Comparable<JRInt> {

  private long val;

  public JRInt(long val) {
    this.val = val;
  }

  public long getVal() {
    return val;
  }

  public void setVal(long val) {
    this.val = val;
  }

  @Override
  public int serialize(OutputStream out) throws IOException {
    out.write(JRType.INT.FLAG_NUMBER);
    EndianUtils.writeSwappedLong(out, val);
    return 9;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    if (stream.read() != JRType.INT.FLAG_NUMBER) {
      throw new JRedisTypeNotMatch("Not a JRInt");
    }
    val = EndianUtils.readSwappedLong(stream);
    return 9;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj) return false;
    if (obj instanceof JRInt jrInt) {
      return jrInt.val == val;
    }
    return false;
  }

  @Override
  public String toString() {
    return "JRInt{" +
        "val=" + val +
        '}';
  }

  public int serialSize() {
    return 9;
  }

  public void increment(long val) {
    this.val += val;
  }

  public void decrement(long val) {
    this.val -= val;
  }

  public void mod(long val) {
    this.val %= val;
  }

  public void multiply(long val) {
    this.val *= val;
  }

  public void divide(long val) {
    this.val /= val;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(val);
  }

  @Override
  public int compareTo(JRInt o) {
    return Long.compare(val, o.val);
  }
}
