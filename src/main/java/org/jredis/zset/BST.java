package org.jredis.zset;

import java.util.List;

public interface BST<K, V> {

  V get(K key);

  V put(K key, V val);

  V remove(K key);

  V update(K key, V val);
  
  boolean contains(K key);

  int getSize();

  List<V> getRange(K low, K high);
}
