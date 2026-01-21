package com.maono.marketapplication.integration;

import com.maono.marketapplication.RedisContainerConfiguration;
import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.redis.util.ProductCache;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@TestConfiguration
@Import({
        IntegrationTestPostgresConfiguration.class,
        RedisContainerConfiguration.class
})
public class IntegrationTestConfiguration {
    @Bean
    public RedisDataManager redisDataManager(ReactiveRedisTemplate<String, PageCache> pageRedisTemplate,
                                             ReactiveRedisTemplate<String, Integer> totalCountRedisTemplate,
                                             ReactiveRedisTemplate<String, ProductCache> productRedisTemplate,
                                             ReactiveRedisTemplate<String , CartItemCache> cartItemRedisTemplate) {
        return new RedisDataManager(pageRedisTemplate, totalCountRedisTemplate, productRedisTemplate, cartItemRedisTemplate);
    }
}
