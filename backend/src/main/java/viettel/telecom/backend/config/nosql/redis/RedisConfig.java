package viettel.telecom.backend.config.nosql.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPooled jedisPooled() {
        // Set up Redis host and port
        String redisHost = "localhost";
        int redisPort = 6377; // Replace with your Redis port

        // Configure Jedis connection pool settings
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(100);        // Maximum number of connections
        poolConfig.setMaxIdle(30);         // Maximum idle connections in the pool
        poolConfig.setMinIdle(10);         // Minimum idle connections in the pool
        poolConfig.setMaxWaitMillis(3000); // Maximum wait time for a connection from the pool (in ms)
        poolConfig.setBlockWhenExhausted(true); // Block if pool is exhausted until a connection is available

        // Return a JedisPooled instance with the configured pool settings
        return new JedisPooled(poolConfig, redisHost, redisPort);
    }
}