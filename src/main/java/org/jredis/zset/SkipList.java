package org.jredis.zset;

import org.apache.commons.io.EndianUtils;
import org.jredis.JRSerializerUtil;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class SkipList<K, V> extends JRedisObject implements BST<K, V> {

  public static int MAX_LEVEL = 32;

  private SkipListNode<K, V> head;

  private final Random random;

  private int level;

  private final Comparator<K> comparator;

  private int size;

  static class SkipListNode<K, V> {

    SkipListNode<K, V> right;

    SkipListNode<K, V> down;

    K key;

    V val;

    public SkipListNode(K key, V val) {
      this.key = key;
      this.val = val;
    }

    public SkipListNode(SkipListNode<K, V> right, SkipListNode<K, V> down, K key, V val) {
      this.right = right;
      this.down = down;
      this.key = key;
      this.val = val;
    }

    public SkipListNode() {}
  }

  public SkipList() {
    this(null);
  }

  public SkipList(Comparator<K> comparator) {
    head = new SkipListNode<>();
    random = new Random();
    level = 1; // start from 1
    size = 0;
    this.comparator = comparator;
  }

  /**
   *
   * @param a
   * @param b
   * @return a < b
   */
  private boolean compare(K a, K b) {
    if (null != comparator)
      return comparator.compare(a, b) < 0;
    var ac = (Comparable) a;
    var bc = (Comparable) b;
    return ac.compareTo(bc) < 0;
  }

  private SkipListNode<K, V> getNode(K key) {
    var n = head;
    while (null != n) {
      // be careful: Node n may be one of the head nodes
      // and the field key of head nodes is null
      if (null != n.key && n.key.equals(key)) {
        return n;
      }
      else if (null == n.right)
        n = n.down;
      else if (compare(key, n.right.key)) // key < n.right.key
        n = n.down;
      else
        n = n.right;
    }
    return null;
  }

  @Override
  public V get(K key) {
    SkipListNode<K, V> node = getNode(key);
    if (null != node)
      return node.val;
    return null;
  }

  @Override
  public V put(K key, V val) {
    SkipListNode<K, V> node = getNode(key);
    if (null != node) {
      V old = node.val;
      node.val = val;
      return old;
    }
    var n = head;
    Stack<SkipListNode<K, V>> stack = new Stack<>();
    while (null != n) {
      if (null == n.right) {
        stack.push(n);
        n = n.down;
      }
      else if (compare(key, n.right.key)) { // key < n.right.key
        stack.push(n);
        n = n.down;
      }else
        n = n.right;
    }
    SkipListNode<K, V> down = null;
    int lvl = 0;
    while (!stack.empty()) {
      node = stack.pop();
      SkipListNode<K, V> newNode = new SkipListNode<>(null, down, key, val);
      newNode.down = down;
      down = newNode;
      newNode.right = node.right;
      node.right = newNode;
      lvl += 1;
      if (lvl > MAX_LEVEL) // reach the max level
        break;
      if (grow()) { // need grow up
        if (lvl == level) {
          level++;
          SkipListNode<K, V> newHead = new SkipListNode<>(null, null, null, null);
          newHead.down = head;
          head = newHead;
          stack.push(head);
        }
      } else {
        break;
      }
    }
    size++;
    return null;
  }

  /**
   * remove a node with {@code key}
   * @return node's value with {@code key}
   */
  @Override
  public V remove(K key) {
    var n = head;
    V ans = null;
    while (null != n) {
      if (n.right == null)
        n = n.down;
      else if (n.right.key.equals(key)) {
        ans = n.right.val;
        var r = n.right;
        n.right = r.right;
        // help gc
        r.right = null;
        r.down = null;
        // remove the nodes below n
        n = n.down;
      } else if (compare(key, n.right.key)) // key < n.right.key
        n = n.down;
      else
        n = n.right;
    }
    size--;
    return ans;
  }

  @Override
  public V update(K key, V val) {
    var node = getNode(key);
    if (null == node) return null;
    V old = node.val;
    node.val = val;
    return old;
  }

  @Override
  public boolean contains(K key) {
    return getNode(key) != null;
  }

  @Override
  public int getSize() {
    return size;
  }

  /**
   * @param low  closed
   * @param high closed
   * @return {@code List<V>}
   */
  @Override
  public List<V> getRange(K low, K high) {
    List<V> ans = new ArrayList<>();
    SkipListNode<K, V> L = lowerBound(low);
    SkipListNode<K, V> R = upperBound(high);
    if (R == null || L == null)
      return List.of();
    assert null == L.down;
    assert null == R.down;
    R = R.right; // closed
    while (L != R) {
      ans.add(L.val);
      L = L.right;
    }
    return ans;
  }

  /**
   * @param key
   * @return Assumption:
   *        1. {@link SkipListNode#down} is {@code null}
   *        2. {@link SkipListNode#key} >= key
   *        3. the previous node is {@code null} or {@link SkipListNode#key} < key
   */
  private SkipListNode<K, V> lowerBound(K key) {
    var n = head;
    SkipListNode<K, V> last = null;
    while (null != n){
      // ensure (last == null or last.val <= key) and
      // (last.right == null or last.right.key > key)
      // when exit this loop
      if (null == n.right) {
        last = n;
        n = n.down;
      }
      else if (compare(key, n.right.key)) { // key < n.right.key
        last = n;
        n = n.down;
      }
      else
        n = n.right;
    }
    if (null == last) return null;
    // last may be one of the heads
    if (null != last.key && last.key.equals(key)) {
      return last;
    }
    if (null == last.right) return null;
    assert compare(key, last.right.key); // n.right.key > key;
    assert last.key == null || compare(last.key, key); // n.key < key
    return last.right;
  }

  /**
   * Multiple key value pairs with the same key is not allowed in {@link SkipListNode} so
   * we just need call {@link SkipListNode#lowerBound(K key)}
   * @param key take value of {@code key}
   * @return Assumption:
   *        1. {@link SkipListNode#down} is {@code null}
   *        2. {@link SkipListNode#key} >= key
   *        3. the next node is {@code null} or {@link SkipListNode#key} > key
   */
  private SkipListNode<K, V> upperBound(K key) {
    return lowerBound(key);
  }

  private boolean grow() {
    return random.nextFloat() > 0.5;
  }

  public int getLevel() {
    return level;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof SkipList<?,?> skipList) {
      if (size != skipList.size) return false;
      var n1 = bottomHead(head);
      n1 = n1.right;
      var n2 = bottomHead((SkipListNode<K, V>)skipList.head);
      n2 = n2.right;
      while (n1 != null && n2 != null) {
        if (!n1.key.equals(n2.key) || !n1.val.equals(n2.val))
          return false;
        n1 = n1.right;
        n2 = n2.right;
      }
      return true;
    }
    return false;
  }

  private SkipListNode<K, V> bottomHead(SkipListNode<K, V> skn) {
    var n = skn;
    while (null != n.down)
      n = n.down;
    return n;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    var n = head;
    while (n.down != null)
      n = n.down;
    n = n.right;
    sb.append("{");
    while (null != n) {
      sb.append(n.key).append(":").append(n.val).append(",");
      n = n.right;
    }
    sb.delete(sb.length() - 1, sb.length());
    sb.append("}");
    return sb.toString();
  }

  @Override
  public int serialSize() throws JRedisTypeNotMatch {
    SkipListNode<K, V> n = bottomHead(head);
    n = n.right;
    int res = 5; // flag(1) + size(4)
    while (null != n) {
      if (n.key instanceof JRedisObject k && n.val instanceof JRedisObject v) {
        res += k.serialSize() + v.serialSize();
        n = n.right;
      }else {
        throw new JRedisTypeNotMatch("only JRedisObject can be serialized");
      }
    }
    return res;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    int off = stream.available();
    if (stream.read() != JRType.ZSET.FLAG_NUMBER) {
      throw new JRedisTypeNotMatch("not a skip list");
    }
    int cnt = EndianUtils.readSwappedInteger(stream);
    size = 0;
    while (cnt-- > 0) {
      var k = JRSerializerUtil.deserialize(stream);
      var v = JRSerializerUtil.deserialize(stream);
      put((K)k, (V)v);
    }
    return off - stream.available();
  }

  @Override
  public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
    out.write(JRType.ZSET.FLAG_NUMBER);
    EndianUtils.writeSwappedInteger(out, size);
    var n = bottomHead(head);
    n = n.right;
    int byteSize = 5;
    while (null != n) {
      if (n.key instanceof JRedisObject k && n.val instanceof JRedisObject v) {
        byteSize += k.serialize(out);
        byteSize += v.serialize(out);
        n = n.right;
      }else {
        throw new JRedisTypeNotMatch("only JRedisObject can be serialized");
      }
    }
    return byteSize;
  }
}
