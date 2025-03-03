package com.banquito.paymentprocessor.validafraude.banquito.config;

import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.ReglaFraudeDTO;
import com.banquito.paymentprocessor.validafraude.banquito.controller.dto.TransaccionTemporalDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableRedisRepositories
@Slf4j
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        log.info("Configurando conexión a Redis: {}:{}", redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public RedisTemplate<String, ReglaFraudeDTO> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        log.info("Configurando RedisTemplate para ReglaFraudeDTO");
        RedisTemplate<String, ReglaFraudeDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        Jackson2JsonRedisSerializer<ReglaFraudeDTO> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, ReglaFraudeDTO.class);
        template.setHashValueSerializer(serializer);
        template.setValueSerializer(serializer);
        
        return template;
    }
    
    @Bean
    public RedisTemplate<String, TransaccionTemporalDTO> transaccionTemporalRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        log.info("Configurando RedisTemplate para TransaccionTemporalDTO");
        RedisTemplate<String, TransaccionTemporalDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        
        Jackson2JsonRedisSerializer<TransaccionTemporalDTO> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, TransaccionTemporalDTO.class);
        template.setValueSerializer(serializer);
        
        return template;
    }
    
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        log.info("Configurando RedisTemplate para Object genérico");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        return template;
    }
} 