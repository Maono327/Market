package com.maono.marketapplication.repositories.redis.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class RedisCartItemRepositoryImpl implements RedisCartItemRepository {
    private final ReactiveRedisTemplate<String, CartItemCache> redisTemplate;

    private static final String key = "cartitem:";
    private static final Integer TTL = 120;

    @Override
    public Mono<CartItem> cacheObject(CartItem object) {
        return redisTemplate
                .opsForValue()
                .set(key + object.getId(),
                        new CartItemCache(object.getId(), object.getCount()),
                        Duration.ofSeconds(TTL))
                .thenReturn(object);
    }

    @Override
    public Mono<Void> dropAllCounts() {
        return redisTemplate.keys(key + "*")
                .flatMap(key -> redisTemplate.opsForValue().get(key))
                .map(cartItemCache -> new CartItem(cartItemCache.id(), 0))
                .flatMap(this::cacheObject)
                .then();
    }

    @Override
    public Mono<CartItem> getCachedObject(Long valueKey) {
        return redisTemplate
                .opsForValue()
                .getAndExpire(key + valueKey, Duration.ofSeconds(TTL))
                .map(cartItemCache -> new CartItem(cartItemCache.id(), cartItemCache.count()));
    }

    @Override
    public Flux<CartItem> multiGet(Collection<Long> ids) {
        return redisTemplate
                .opsForValue()
                .multiGet(ids.stream().map(id -> key + id).toList())
                .map(cartItemCaches -> cartItemCaches.stream().filter(Objects::nonNull).toList())
                .flatMapMany(Flux::fromIterable)
                .map(cartItemCache -> new CartItem(
                        cartItemCache.id(),
                        cartItemCache.count()
                ))
                .flatMap(this::cacheObject);
    }
}
