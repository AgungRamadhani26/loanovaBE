package com.example.loanova.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Registrasi modul untuk handling Java 8 Time (LocalDateTime, dll)
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Konfigurasi visibilitas agar Jackson bisa akses field tanpa getter/setter jika perlu
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    // PENTING: Mengaktifkan default typing agar info kelas disimpan dalam JSON (@class)
    // Ini solusi untuk error ClassCastException pada List
    PolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
    mapper.activateDefaultTyping(
        ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    return mapper;
  }

  @Bean
  public RedisCacheManager cacheManager(
      RedisConnectionFactory factory, ObjectMapper redisObjectMapper) {
    // Gunakan serializer yang sudah dikonfigurasi dengan ObjectMapper kita
    GenericJackson2JsonRedisSerializer serializer =
        new GenericJackson2JsonRedisSerializer(redisObjectMapper);

    // 1. Konfigurasi DEFAULT (Misalnya 1 jam)
    RedisCacheConfiguration defaultConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    // 2. Konfigurasi Khusus per Nama Cache (Custom TTL)
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // Mengatur TTL 20 Menit untuk User
    RedisCacheConfiguration userCacheConfig = defaultConfig.entryTtl(Duration.ofMinutes(20));
    cacheConfigurations.put("user", userCacheConfig);
    cacheConfigurations.put("users", userCacheConfig);

    // 3. Konfigurasi untuk Branch (10 Menit)
    RedisCacheConfiguration branchCacheConfig = defaultConfig.entryTtl(Duration.ofMinutes(10));
    cacheConfigurations.put("branch", branchCacheConfig);
    cacheConfigurations.put("branches", branchCacheConfig);

    return RedisCacheManager.builder(factory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
  }
}
