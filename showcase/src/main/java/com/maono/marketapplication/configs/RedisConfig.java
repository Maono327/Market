package com.maono.marketapplication.configs;

import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.redis.util.ProductCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Integer> totalCountReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        RedisSerializer<Integer> integerSerializer = new GenericToStringSerializer<>(Integer.class);
        RedisSerializationContext<String, Integer> context =
                RedisSerializationContext.<String, Integer>newSerializationContext(keySerializer)
                        .value(integerSerializer)
                        .hashKey(keySerializer)
                        .hashValue(integerSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, PageCache> pageReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<PageCache> pageCacheSerializer =
                new Jackson2JsonRedisSerializer<>(PageCache.class);
        RedisSerializationContext<String, PageCache> context =
                RedisSerializationContext.<String, PageCache>newSerializationContext()
                        .key(stringRedisSerializer)
                        .hashKey(stringRedisSerializer)
                        .value(pageCacheSerializer)
                        .hashValue(pageCacheSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, ProductCache> productReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<ProductCache> productRedisSerializer =
                new Jackson2JsonRedisSerializer<>(ProductCache.class);
        RedisSerializationContext<String, ProductCache> context =
                RedisSerializationContext.<String, ProductCache>newSerializationContext()
                        .key(stringRedisSerializer)
                        .hashKey(stringRedisSerializer)
                        .value(productRedisSerializer)
                        .hashValue(productRedisSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, CartItemCache> cartItemReactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<CartItemCache> cartItemRedisSerializer =
                new Jackson2JsonRedisSerializer<>(CartItemCache.class);
        RedisSerializationContext<String, CartItemCache> context =
                RedisSerializationContext.<String, CartItemCache>newSerializationContext()
                        .key(stringRedisSerializer)
                        .hashKey(stringRedisSerializer)
                        .value(cartItemRedisSerializer)
                        .hashValue(cartItemRedisSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
