package com.example.cashflow_monitoring.config;

import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, CompanyInfo> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        RedisSerializationContext<String, CompanyInfo> serializationContext = RedisSerializationContext
                .<String, CompanyInfo>newSerializationContext(new StringRedisSerializer())
                .hashKey(new GenericToStringSerializer<>(Integer.class))
                .hashValue(new Jackson2JsonRedisSerializer<>(CompanyInfo.class))
                .build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }
//    @Bean
//    public ReactiveRedisTemplate<String, CompanyInfo> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
//        Jackson2JsonRedisSerializer<CompanyInfo> companyInfoSerializer = new Jackson2JsonRedisSerializer<>(CompanyInfo.class);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        companyInfoSerializer.setObjectMapper(objectMapper);
//
//        // Use StringRedisSerializer for keys
//        RedisSerializationContext<String, CompanyInfo> serializationContext = RedisSerializationContext
//                .<String, CompanyInfo>newSerializationContext(new StringRedisSerializer())
//                .value(companyInfoSerializer) // Set serializer for the values
//                .build();
//
//        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
//    }

}
