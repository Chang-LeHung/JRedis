package org.jredis.hash;

public class JRHash<K, V> implements Hash<K, V> {

  static class HashEntry<K, V> {
    final K key;
    V val;
    HashEntry<K, V> nxt;

    public HashEntry(K key) {
      this.key = key;
    }

    public HashEntry(K key, V val) {
      this.key = key;
      this.val = val;
    }
  }

  public static final int MAX_SIZE = 0x0ffffffe;

  public static final int DEFAULT_SIZE = 16;

  public static final double LOAD_FACTOR = .75;

  int mask;

  /** {@link JRIncrementalHash} will use the field so using default access flag. */
  HashEntry<K, V>[] buckets;

  int size;
  private final double lf;

  public JRHash(int len) {
    this(len, LOAD_FACTOR);
  }

  public JRHash(int len, double loadFactor) {
    if (len < 2) len = 2;
    len = roundUp(len);
    len = Math.min(len, MAX_SIZE);
    buckets = new HashEntry[len];
    size = 0;
    lf = loadFactor;
    mask = len - 1;
  }

  public JRHash() {
    buckets = new HashEntry[DEFAULT_SIZE];
    size = 0;
    lf = LOAD_FACTOR;
    mask = DEFAULT_SIZE - 1;
  }

  public JRHash(double loadFactor) {
    this.lf = loadFactor;
    buckets = new HashEntry[DEFAULT_SIZE];
    size = 0;
    mask = DEFAULT_SIZE - 1;
  }

  public int getSize() {
    return size;
  }

  private void resize() {
    if (size > MAX_SIZE / 2) throw new RuntimeException("HashMap is full, cannot resize");
    int newSize = buckets.length << 1;
    var oldBuckets = buckets;
    buckets = new HashEntry[newSize];
    size = 0;
    for (HashEntry<K, V> entry : oldBuckets) {
      while (null != entry) {
        put(entry.key, entry.val);
        entry = entry.nxt;
      }
    }
    mask = newSize - 1;
  }

  /**
   * @param key if exists, update the old value and return it
   * @param val
   * @return
   */
  @Override
  public V put(K key, V val) {
    int pos = getPos(key);
    HashEntry<K, V> e = buckets[pos];
    if (null == e) {
      HashEntry<K, V> entry = new HashEntry<>(key, val);
      buckets[pos] = entry;
      size++;
      if (size > buckets.length * lf) {
        resize();
      }
      return null;
    } else {
      HashEntry<K, V> prev = null;
      while (e != null && !e.key.equals(key)) {
        prev = e;
        e = e.nxt;
      }
      if (null == e) {
        prev.nxt = new HashEntry<>(key, val);
        size++;
        if (size > buckets.length * lf) {
          resize();
        }
        return null;
      }
      // update old val to new one
      V old = e.val;
      e.val = val;
      return old;
    }
  }

  private int getPos(K key) {
    assert key != null;
    int hash = key.hashCode();
    return hash & mask;
  }

  @Override
  public V get(K key) {
    int pos = getPos(key);
    HashEntry<K, V> e = getHashEntry(key, pos);
    if (null == e) return null;
    return e.val;
  }

  private HashEntry<K, V> getHashEntry(K key, int pos) {
    var e = buckets[pos];
    if (null == e) return null;
    while (null != e && !e.key.equals(key)) {
      e = e.nxt;
    }
    return e;
  }

  @Override
  public V remove(K key) {
    int pos = getPos(key);
    HashEntry<K, V> e = buckets[pos];
    HashEntry<K, V> prev = null;
    while (null != e && !e.key.equals(key)) {
      prev = e;
      e = e.nxt;
    }
    if (null != e) {
      if (null == prev) {
        buckets[pos] = e.nxt;
      } else {
        prev.nxt = e.nxt;
      }
      size--;
      return e.val;
    }
    return null;
  }

  public V update(K key, V val) {
    return put(key, val);
  }

  public static int roundUp(int l) {
    assert l > 1;
    l -= 1;
    l |= l >> 1;
    l |= l >> 2;
    l |= l >> 4;
    l |= l >> 8;
    l |= l >> 16;
    l = Math.min(l, MAX_SIZE);
    return l + 1;
  }

  @Override
  public boolean contains(K key) {
    int pos = getPos(key);
    HashEntry<K, V> e = getHashEntry(key, pos);
    return null != e;
  }
}
