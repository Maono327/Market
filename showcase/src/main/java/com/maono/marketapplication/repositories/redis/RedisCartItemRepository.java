package com.maono.marketapplication.repositories.redis;

import com.maono.marketapplication.models.CartItem;
import reactor.core.publisher.Mono;

public interface RedisCartItemRepository extends RedisCRRepository<CartItem> {
    Mono<Void> dropAllCounts();
}
