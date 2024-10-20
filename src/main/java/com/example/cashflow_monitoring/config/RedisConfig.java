package com.example.cashflow_monitoring.config;

import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
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

}
