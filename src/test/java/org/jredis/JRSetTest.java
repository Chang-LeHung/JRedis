package org.jredis;

import org.jredis.exception.JRedisTypeNotMatch;
import org.jredis.number.JRInt;
import org.jredis.set.JRSet;
import org.jredis.string.JRString;
import org.jredis.zset.SkipList;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author：Henry Wan
 * @Package：org.jredis
 * @Project：JRedis
 * @Date：2024/4/16 21:37
 * @Filename：JRSetTest
 */
public class JRSetTest {
    public static TestLogger logger = new TestLogger(JRSet.class);

    @Test
    public void testJRSet() throws IOException, JRedisTypeNotMatch {
        JRSet<JRString> jrset = new JRSet();
        for (int i = 0; i < 9; i++) {
            jrset.add(new JRString(String.valueOf(i)));
        }
        logger.pass(jrset);
        assert jrset.getSize() == 9;
        logger.pass("getSize() passed");
        logger.pass("add() passed");
        for (int i = 0; i < 4; i++) {
            jrset.remove(new JRString(String.valueOf(i)));
        }
        logger.pass(jrset);
        assert jrset.getSize() == 5;
        logger.pass("getSize() passed");
        logger.pass("remove() passed");
        for (int i = 0; i < 4; i++) {
            assert !jrset.contains(new JRString(String.valueOf(i)));
        }
        for (int i = 5; i < 9; i++) {
            assert jrset.contains(new JRString(String.valueOf(i)));
        }
        logger.pass("contains() passed");
        jrset.clear();
        logger.pass(jrset);
        assert jrset.getSize() == 0;
        logger.pass("getSize() passed");
        logger.pass("clear() passed");
    }

    @Test
    public void testSerialization() throws JRedisTypeNotMatch, IOException {
        JRSet<JRString> origin = new JRSet<>();
        for (int i = 0; i < 10; i++) {
            origin.add(new JRString(String.valueOf(i)));
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        origin.serialize(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        JRSet<JRString> deserialized = new JRSet<>();
        deserialized.deserialize(inputStream);
        assert origin.getSize() == deserialized.getSize();
        logger.pass("serialSize() passed");
        for (JRString element : origin) {
            assert deserialized.contains(element);
        }
        logger.pass("serialize() passed");
        logger.pass("deserialize() passed");
    }


}
