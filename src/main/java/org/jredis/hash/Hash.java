package org.jredis.hash;

public interface Hash<K, V> {

  V remove(K key);

  V put(K key, V val);

  V get(K key);

  boolean contains(K key);
}
