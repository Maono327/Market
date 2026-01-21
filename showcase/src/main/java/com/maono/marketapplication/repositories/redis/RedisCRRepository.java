package com.maono.marketapplication.repositories.redis;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RedisCRRepository<T> {
    Mono<T> cacheObject(T object);
    Mono<T> getCachedObject(Long valueKey);
    Flux<T> multiGet(Collection<Long> ids);
}
