package org.jredis.hash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.EndianUtils;
import org.jredis.JRSerializerUtil;
import org.jredis.JRType;
import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

public class JRIncrementalHash<K extends JRedisObject, V extends JRedisObject> extends JRedisObject
    implements Hash<K, V> {

  public static long MAX_REHASH_INTERVAL = 10; // ms

  private final JRHash<K, V>[] dt;

  private final double lf;
  /**
   * 1. rehashIndex == -1 means not in rehashing process 2. rehashIndex >= 0 means in rehashing
   * process and the next rehashing index is rehashIndex.
   */
  private int rehashIndex;

  /**
   * The purpose of the {@link JRIncrementalHash#lf} is 1/2 of the load factor in {@link JRHash} is
   * to avoid the resize operation. We will do rehashing before that.
   *
   * @param len bucket length
   * @param lf load factor
   */
  public JRIncrementalHash(int len, double lf) {
    this.lf = lf;
    dt = new JRHash[2];
    dt[0] = new JRHash<>(len, lf * 2);
    rehashIndex = -1;
  }

  public JRIncrementalHash(double lf) {
    this.lf = lf;
    dt = new JRHash[2];
    dt[0] = new JRHash<>(lf * 2);
    rehashIndex = -1;
  }

  public JRIncrementalHash(int len) {
    lf = JRHash.LOAD_FACTOR;
    dt = new JRHash[2];
    dt[0] = new JRHash<>(len, lf * 2);
    rehashIndex = -1;
  }

  public JRIncrementalHash() {
    this.lf = JRHash.LOAD_FACTOR;
    dt = new JRHash[2];
    dt[0] = new JRHash<K, V>(lf * 2);
    rehashIndex = -1;
  }

  @Override
  public V remove(K key) {
    if (rehashIndex == -1) {
      // in normal state
      return dt[0].remove(key);
    } else {
      // in rehashing process
      doIncrementalHashStep();
      // check the rehashing index first
      if (rehashIndex == -1)
        return remove(key);
      int pos = key.hashCode() & dt[0].mask;
      if (pos < rehashIndex) {
        return dt[1].remove(key);
      } else {
        return dt[0].remove(key);
      }
    }
  }

  @Override
  public V put(K key, V val) {
    if (rehashIndex == -1) {
      V ret = dt[0].put(key, val);
      if (dt[0].size > dt[0].buckets.length * lf) {
        rehashIndex = 0;
        int minSize = Math.min(dt[0].buckets.length << 1, JRHash.MAX_SIZE + 1);
        dt[1] = new JRHash<>(minSize, lf * 2);
        doIncrementalHashStep();
      }
      return ret;
    } else {
      doIncrementalHashStep();
      // after rehashing, dt is possible to be normal state
      // so check it first
      if (rehashIndex == -1)
        return put(key, val);
      int pos = key.hashCode() & dt[0].mask;
      // if exists in the old table, remove it first
      if (pos >= rehashIndex) {
        dt[0].remove(key);
      }
      return dt[1].put(key, val);
    }
  }

  private void doIncrementalHashStep() {
    assert rehashIndex >= 0 && dt[1] != null;
    int len = dt[0].buckets.length;
    long start = System.currentTimeMillis();
    while (rehashIndex < len) {
      doReHashing();
      long end = System.currentTimeMillis();
      if (end - start > MAX_REHASH_INTERVAL) break;
    }
    if (rehashIndex == dt[0].buckets.length) {
      // back to normal state
      rehashIndex = -1;
      dt[0] = dt[1];
      dt[1] = null;
    }
  }

  /** do a rehashing operation only forward one step */
  private void doReHashing() {
    var bkt = dt[0].buckets;
    JRHash.HashEntry<K, V> e;
    int size = 0;
    do {
      e = bkt[rehashIndex];
      if (e != null) {
        size++;
        dt[1].put(e.key, e.val);
        bkt[rehashIndex] = e.nxt;
      }
    } while (e != null);
    bkt[rehashIndex] = null; // help gc
    dt[0].size -= size; // update entry size
    rehashIndex++;
  }

  @Override
  public V get(K key) {
    if (rehashIndex == -1) {
      return dt[0].get(key);
    } else {
      // forward at least a step of rehashing.
      doIncrementalHashStep();
      // check first
      if (rehashIndex == -1)
        return get(key);
      int pos = key.hashCode() & dt[0].mask;
      if (pos < rehashIndex) {
        return dt[1].get(key);
      } else {
        return dt[0].get(key);
      }
    }
  }

  private static <K extends JRedisObject, V extends JRedisObject> int serializeHash(
      JRHash<K, V> h, OutputStream stream) throws IOException, JRedisTypeNotMatch {
    var bkt = h.buckets;
    int size = 0;
    for (var e : bkt) {
      while (null != e) {
        size += e.key.serialize(stream);
        size += e.val.serialize(stream);
        e = e.nxt;
      }
    }
    return size;
  }

  private static <K extends JRedisObject, V extends JRedisObject> int serializeHashSize(
      JRHash<K, V> h) throws JRedisTypeNotMatch {
    var bkt = h.buckets;
    int size = 0;
    for (var e : bkt) {
      while (null != e) {
        size += e.key.serialSize();
        size += e.val.serialSize();
        e = e.nxt;
      }
    }
    return size;
  }

  @Override
  public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
    out.write(JRType.HASH.FLAG_NUMBER);
    int size = 5;
    int cnt;
    if (rehashIndex == -1) {
      cnt = dt[0].size;
      EndianUtils.writeSwappedInteger(out, cnt);
      size += serializeHash(dt[0], out);
    } else {
      cnt = dt[0].size + dt[1].size;
      EndianUtils.writeSwappedInteger(out, cnt);
      size += serializeHash(dt[0], out);
      size += serializeHash(dt[1], out);
    }
    return size;
  }

  @Override
  public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
    int off = stream.available();
    if (stream.read() != JRType.HASH.FLAG_NUMBER)
      throw new JRedisTypeNotMatch("hash type flag not match");
    int size = EndianUtils.readSwappedInteger(stream);
    dt[0] = null;
    dt[1] = null;
    int len = JRHash.roundUp((int) (size / lf));
    dt[0] = new JRHash<>(len, lf * 2);
    for (int i = 0; i < size; i++) {
      JRedisObject key = JRSerializerUtil.deserialize(stream);
      JRedisObject val = JRSerializerUtil.deserialize(stream);
      put((K) key, (V) val);
    }
    return off - stream.available();
  }

  @Override
  public int serialSize() throws JRedisTypeNotMatch {
    if (rehashIndex == -1) {
      return 5 + serializeHashSize(dt[0]);
    }
    return 5 + serializeHashSize(dt[0]) + serializeHashSize(dt[1]);
  }

  public boolean inReHash() {
    return rehashIndex == -1;
  }

  public int getSize() {
    if (rehashIndex == -1)
      return dt[0].size;
    return dt[0].size + dt[1].size;
  }

  @Override
  public boolean contains(K key) {
    if (rehashIndex == -1) {
      return dt[0].contains(key);
    } else {
      doIncrementalHashStep();
      int pos = key.hashCode() & dt[0].mask;
      if (pos < rehashIndex) {
        return dt[1].contains(key);
      } else {
        return dt[0].contains(key);
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj) return false;
    if (obj instanceof JRIncrementalHash hash) {
      if (hash.getSize() != getSize()) return false;
      if (!equalsCheck(dt[0].buckets, hash)) return false;
      if (null != dt[1])
        return equalsCheck(dt[1].buckets, hash);
      return true;
    }
    return false;
  }

  private boolean equalsCheck(JRHash.HashEntry<K, V>[] buckets, JRIncrementalHash<K, V> hash) {
    for (var entry : buckets) {
      while (null != entry) {
        JRedisObject retVal = hash.get(entry.key);
        if (null != retVal && !retVal.equals(entry.val))
          return false;
        entry = entry.nxt;
      }
    }
    return true;
  }
}
