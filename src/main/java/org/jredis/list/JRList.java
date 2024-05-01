package org.jredis.list;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.EndianUtils;
import org.jredis.JRSerializerUtil;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisOutOfBound;
import org.jredis.exception.JRedisTypeNotMatch;

public class JRList extends JRedisObject {

  private final Node head;
  private int size;

  public JRList() {
    head = new Node(null);
    reinitialize();
  }

  JRedisObject popLeft() {
    if (size == 0) return null;
    var ret = head.next;
    head.next = ret.next;
    ret.next.prev = head;
    size--;
    return ret.obj;
  }

  JRedisObject popRight() {
    if (size == 0) return null;
    var ret = head.prev;
    head.prev = ret.prev;
    ret.prev.next = head;
    size--;
    return ret.obj;
  }

  public void appendRight(JRedisObject o) {
    Node node = new Node(o);
    var tail = head.prev;
    node.next = head;
    node.prev = tail;
    tail.next = node;
    head.prev = node;
    size++;
  }

  public void appendLeft(JRedisObject o) {
    Node node = new Node(o);
    var nxt = head.next;
    node.prev = head;
    node.next = head.next;
    head.next = node;
    nxt.prev = node;
    size++;
  }

  @Override
  public byte[] serialize() throws JRedisTypeNotMatch {
    var stream = new ByteArrayOutputStream();
    try {
      serializeToStream(stream);
    } catch (IOException ignore) {
    }
    return stream.toByteArray();
  }

  @Override
  public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
    return serializeToStream(out);
  }

  private int serializeToStream(OutputStream out) throws IOException, JRedisTypeNotMatch {
    int s = 5;
    out.write(JRType.LIST.FLAG_NUMBER);
    EndianUtils.writeSwappedInteger(out, size);
    var n = head.next;
    while (n != head) {
      var o = n.obj;
      try {
        // fast path
        s += o.serialize(out);
      } catch (IOException | UnsupportedOperationException e) {
        // fall back to slow path
        byte[] data = o.serialize();
        out.write(data, 0, data.length);
        s += data.length;
      }
      n = n.next;
    }
    return s;
  }

  private void reinitialize() {
    head.next = head;
    head.prev = head;
    size = 0;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    assert stream != null;
    reinitialize();
    int off = stream.available();
    if (stream.read() != JRType.LIST.FLAG_NUMBER)
      throw new JRedisTypeNotMatch("require a list data");
    int cnt = EndianUtils.readSwappedInteger(stream);
    for (int i = 0; i < cnt; i++) {
      JRedisObject o = JRSerializerUtil.deserialize(stream);
      appendRight(o);
    }
    assert size == cnt;
    return off - stream.available();
  }

  /**
   * Do not call this function frequently(time complexity O(n))
   *
   * @return acquired byte array size in serialization
   */
  @Override
  public int serialSize() throws JRedisTypeNotMatch {
    int size = 5;
    var node = head.next;
    while (node != head) {
      size += node.obj.serialSize();
      node = node.next;
    }
    return size;
  }

  @Override
  public JRType getType() {
    return JRType.LIST;
  }

  public JRedisObject getByIndex(int idx) throws JRedisOutOfBound {
    if (idx < size && idx >= 0) {
      var t = head;
      while (size-- > 0) {
        t = t.next;
      }
      return t.obj;
    }
    throw new JRedisOutOfBound("index " + idx + " out of bound " + size);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (var n = head.next; n != head; n = n.next) {
      sb.append(n.obj.toString());
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length());
    sb.append("]");
    return sb.toString();
  }

  public int getSize() {
    return size;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj) return false;
    if (obj instanceof JRList list) {
      if (size != list.size) return false;
      for (Node n = head.next, m = list.head.next; n != head; n = n.next, m = m.next) {
        if (!n.obj.equals(m.obj)) return false;
      }
      return true;
    }
    return false;
  }

  private static class Node {
    private final JRedisObject obj;
    private Node prev;
    private Node next;

    public Node(JRedisObject obj) {
      this.obj = obj;
    }
  }
}
