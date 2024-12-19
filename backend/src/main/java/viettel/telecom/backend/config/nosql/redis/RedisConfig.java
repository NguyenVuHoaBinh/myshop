package viettel.telecom.backend.config.nosql.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.validation.constraints.NotNull;

/**
 * Redis configuration with connection pooling for Lettuce.
 */
@Configuration
public class RedisConfig {

    @Value("${redis.host:#{localhost}}")
    @NotNull
    private String host;


    @Value("${redis.port}")
    private int port;

    @Value("${redis.setMaxTotal}")
    private int maxTotal;

    @Value("${redis.setMaxIdle}")
    private int maxIdle;

    @Value("${redis.setMinIdle}")
    private int minIdle;

    @Value("${redis.commandTimeout}")
    private long commandTimeout;

    @Value("${redis.shutdownTimeout}")
    private long shutdownTimeout;

    /**
     * Provides thread-safe client resources for Lettuce.
     *
     * @return DefaultClientResources
     */
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources clientResources() {
        return DefaultClientResources.create();
    }

    /**
     * Configures a LettuceConnectionFactory with connection pooling.
     *
     * @param clientResources shared client resources
     * @return LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(DefaultClientResources clientResources) {
        // Redis server configuration
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);

        // Connection pool configuration
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal); // Max active connections
        poolConfig.setMaxIdle(maxIdle);  // Max idle connections
        poolConfig.setMinIdle(minIdle);   // Min idle connections

        // Lettuce client configuration with pooling
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(java.time.Duration.ofMillis(commandTimeout))
                .shutdownTimeout(java.time.Duration.ofMillis(shutdownTimeout))
                .clientResources(clientResources)
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    /**
     * Configures a RedisTemplate with custom serializers.
     *
     * @param lettuceConnectionFactory the connection factory
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        // Use Jackson JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
