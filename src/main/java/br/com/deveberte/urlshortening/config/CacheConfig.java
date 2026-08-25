package br.com.deveberte.urlshortening.config;

import br.com.deveberte.urlshortening.domain.entity.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer
{
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheTtlCustomizer(RedisCacheConfiguration base) {
        return builder -> builder
                .withCacheConfiguration("urls", base
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new JacksonJsonRedisSerializer<>(Link.class))));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("cache GET falhou em '{}' key={}: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("cache PUT falhou em '{}' key={}: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("cache EVICT falhou em '{}' key={}: {}", cache.getName(), key, e.getMessage());
            }
            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("cache CLEAR falhou em '{}': {}", cache.getName(), e.getMessage());
            }
        };
    }
}
