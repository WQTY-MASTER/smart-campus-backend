package com.qfedu.smartcampusseckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 核心修改：配置ObjectMapper，注册JavaTimeModule支持LocalDateTime序列化
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块，解决LocalDateTime/LocalDate序列化问题
        objectMapper.registerModule(new JavaTimeModule());
        // 创建支持日期类型的JSON序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key 使用 String 序列化（不变）
        template.setKeySerializer(new StringRedisSerializer());
        // Value 使用配置好的JSON序列化器（替换原来的默认实例）
        template.setValueSerializer(jsonSerializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}