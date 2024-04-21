package org.jredis;


import java.io.InputStream;
import java.util.Map;

import org.jredis.server.JRedisConfiguration;
import org.jredis.server.JRedisServer;
import org.yaml.snakeyaml.Yaml;

public class JRedisEntry {

    private static final String config = "properties.yml";

    private static JRedisConfiguration loadConfiguration() {
        Yaml yaml = new Yaml();
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(config);
        Map<String, Object> config = yaml.load(inputStream);
        return new JRedisConfiguration(config);
    }
    
    public static void main(String[] args) throws Exception {
        var config = loadConfiguration();
        RedisDatabase.initRedisDatabase();
        JRedisServer server = new JRedisServer(config);
        server.eventLoop();
    }
}
