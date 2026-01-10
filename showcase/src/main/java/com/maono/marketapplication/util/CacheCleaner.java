package com.maono.marketapplication.util;

import com.maono.marketapplication.repositories.redis.util.ProductCache;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class CacheCleaner {
    private final ReactiveRedisTemplate<String, ProductCache> redisTemplate;

    public Mono<Void> cleanCache() {
        return redisTemplate.execute(connection -> connection.serverCommands().flushAll()).then();
    }
}
