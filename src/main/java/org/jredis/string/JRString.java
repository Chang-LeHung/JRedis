package org.jredis.string;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.Getter;
import org.apache.commons.io.EndianUtils;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.hash.JRHash;

public class JRString extends JRedisObject implements Comparable<JRString> {

  public static int DEFAULT_SIZE = 8;

  private byte[] buf;

  @Getter
  private int size;

  public JRString(String s) {
    byte[] arr = s.getBytes();
    buf = new byte[JRHash.roundUp(arr.length)];
    System.arraycopy(arr, 0, buf, 0, arr.length);
    size = arr.length;
  }

  public JRString() {
    buf = new byte[DEFAULT_SIZE];
    size = 0;
  }

  @Override
  public int serialSize() {
    return size + 5;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JRString jrString)) return false;
    if (size != jrString.size) return false;
    for(int i = 0; i < size; i++)
      if (buf[i] != jrString.buf[i]) return false;
    return true;
  }

  /**
   * JRString memory layout
   * layout       : |type|size|data|
   * length(byte) : |  1 |  4 |size|
   *
   * @return byte[]
   */
  @Override
  public int serialize(OutputStream out) throws IOException {
    out.write(JRType.STRING.FLAG_NUMBER);
    EndianUtils.writeSwappedInteger(out, size);
    out.write(buf, 0, size);
    return size + 5;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    int off = stream.available();
    if (stream.read() != JRType.STRING.FLAG_NUMBER)
      throw new JRedisTypeNotMatch("require a string data");
    size = EndianUtils.readSwappedInteger(stream);
    buf = new byte[JRHash.roundUp(size)];
    int s = stream.read(buf, 0, size);
    if (size != s) throw new JRedisTypeNotMatch("broken list data, not enough data");
    return off - stream.available();
  }

  /**
   * This hash function is a copy of {@link StringLatin1#hashCode}
   *
   * @return hashcode
   */
  @Override
  public int hashCode() {
    int h = 0;
    for (byte v : buf) {
      h = 31 * h + (v & 0xff);
    }
    return h;
  }

  public int append(String s) {
    var arr = s.getBytes();
    int newSize = size + arr.length;
    if (buf.length >= newSize)
      System.arraycopy(arr, 0, buf, size, arr.length);
    else {
      byte[] newBuf = new byte[JRHash.roundUp(newSize)];
      System.arraycopy(buf, 0, newBuf, 0, size);
      System.arraycopy(arr, 0, newBuf, size, arr.length);
      buf = newBuf;
    }
    size += arr.length;
    return size;
  }

  @Override
  public String toString() {
    return "JRString{" + new String(buf, 0, size) + '}';
  }

  public String bufToString() {
    return new String(buf);
  }

  @Override
  public int compareTo(JRString o) {
    int minSize = Math.min(size, o.size);
    for(int i = 0; i < minSize; i++)
      if (buf[i] != o.buf[i])
        return buf[i] - o.buf[i];
    return size - o.size;
  }
}
