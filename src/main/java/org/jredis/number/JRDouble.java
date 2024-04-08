package org.jredis.number;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.EndianUtils;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

public class JRDouble extends JRedisObject implements Comparable<JRDouble> {

  private double val;

  public JRDouble(double val) {
    this.val = val;
  }

  public int serialSize() {
    return 9;
  }

  @Override
  public int serialize(OutputStream out) throws IOException {
    out.write(JRType.DOUBLE.FLAG_NUMBER);
    EndianUtils.writeSwappedDouble(out, val);
    return 9;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    if (stream.read() != JRType.DOUBLE.FLAG_NUMBER)
      throw new JRedisTypeNotMatch("require a double object");
    val = EndianUtils.readSwappedDouble(stream);
    return 9;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JRDouble other) {
      return val == other.val;
    }
    return false;
  }

  @Override
  public String toString() {
    return "JRDouble{" +
        "val=" + val +
        '}';
  }

  public double getVal() {
    return val;
  }

  public void setVal(double val) {
    this.val = val;
  }

  public void increment(double val) {
    this.val += val;
  }

  public void decrement(double val) {
    this.val -= val;
  }

  public void multiply(double val) {
    this.val *= val;
  }

  public void divide(double val) {
    this.val /= val;
  }

  public void mod(double val) {
    this.val %= val;
  }

  @Override
  public int hashCode() {
    return Double.hashCode(val);
  }

  @Override
  public int compareTo(JRDouble o) {
   return Double.compare(val, o.val);
  }

  public String valueToString() {
    return String.valueOf(val);
  }
}
