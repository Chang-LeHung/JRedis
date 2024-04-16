package org.jredis.set;

import org.jredis.JRedisObject;
import org.jredis.exception.JRedisTypeNotMatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class JRSet<K extends JRedisObject> extends JRedisObject implements Iterable<K> {
    private HashSet<K> set = new HashSet<>();

    // 添加元素
    public boolean add(K element) {
        return set.add(element);
    }

    // 移除元素
    public boolean remove(K element) {
        return set.remove(element);
    }

    // 检查元素是否存在
    public boolean contains(K element) {
        return set.contains(element);
    }

    // 返回集合大小
    public int getSize() {
        return set.size();
    }

    // 清空集合
    public void clear() {
        set.clear();
    }

    @Override
    public String toString() {
        return "JRSet{" + set.stream()
                .map(K::toString)
                .collect(Collectors.joining(", ", "[", "]")) + '}';
    }
    @Override
    public Iterator<K> iterator() {
        return set.iterator();
    }
    @Override
    public int serialize(OutputStream out) throws IOException, JRedisTypeNotMatch {
        return super.serialize(out);
    }

    @Override
    public int deserialize(InputStream stream) throws IOException, JRedisTypeNotMatch {
        return super.deserialize(stream);
    }

    @Override
    public int serialSize() throws JRedisTypeNotMatch {
        return super.serialSize();
    }
}

