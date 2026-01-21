package com.maono.marketapplication.repositories.redis;

import com.maono.marketapplication.models.Product;
import reactor.core.publisher.Mono;

public interface RedisProductRepository extends RedisCRRepository<Product> {
    Mono<Integer> cacheTotalCount(Integer totalCount, String search);
    Mono<Integer> getCachedTotalCount(String search);
}
