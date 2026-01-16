package com.wiley.luo.springcloudclientdemo.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@EnableCaching  // 启用缓存支持
public class RedisConfig {
    
    /**
     * 自定义 RedisTemplate
     * 设置序列化方式，避免在Redis中看到乱码
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 设置key的序列化方式
        template.setKeySerializer(keySerializer());
        template.setHashKeySerializer(keySerializer());
        
        // 设置value的序列化方式
        template.setValueSerializer(valueSerializer());
        template.setHashValueSerializer(valueSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 自定义 StringRedisTemplate（处理字符串）
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        return template;
    }
    
    /**
     * Key序列化器
     */
    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }
    
    /**
     * Value序列化器（使用JSON序列化）
     */
    private RedisSerializer<Object> valueSerializer() {
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置序列化的规则
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        serializer.setObjectMapper(objectMapper);
        
        return serializer;
    }
    
    /**
     * 配置缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 默认配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            // 设置缓存有效期1小时
            .entryTtl(Duration.ofHours(1))
            // 禁用缓存空值
            .disableCachingNullValues()
            // 设置key序列化
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(keySerializer()))
            // 设置value序列化
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(valueSerializer()));
        
        // 构建缓存管理器
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            // 可以对不同的cacheName设置不同的配置
            .withCacheConfiguration("users", 
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("products",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(2)))
            .transactionAware()
            .build();
    }
}