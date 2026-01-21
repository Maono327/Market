package com.maono.marketapplication.repositories.redis.implementations;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.RedisProductRepository;
import com.maono.marketapplication.repositories.redis.util.ProductCache;
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
public class RedisProductRepositoryImpl implements RedisProductRepository {
    private final ReactiveRedisTemplate<String, ProductCache> redisProductTemplate;
    private final ReactiveRedisTemplate<String, Integer> redisTotalCountTemplate;

    private static final String key = "product:";
    private static final Integer TTL = 120;

    @Override
    public Mono<Product> cacheObject(Product object) {
        ProductCache cache = new ProductCache(
                object.getId(),
                object.getTitle(),
                object.getDescription(),
                object.getImageName(),
                object.getPrice()
        );

        return redisProductTemplate.opsForValue()
                .set(key + object.getId(), cache, Duration.ofSeconds(TTL))
                .thenReturn(object);
    }

    @Override
    public Mono<Product> getCachedObject(Long valueKey) {
        return redisProductTemplate.opsForValue().getAndExpire(key + valueKey, Duration.ofSeconds(TTL))
                .map(productCache -> new Product(
                        productCache.id(),
                        productCache.title(),
                        productCache.description(),
                        productCache.imageName(),
                        null,
                        productCache.price())
                );
    }

    @Override
    public Flux<Product> multiGet(Collection<Long> ids) {
        return redisProductTemplate
                .opsForValue()
                .multiGet(ids.stream().map(id -> key + id).toList())
                .map(productCaches -> productCaches.stream().filter(Objects::nonNull).toList())
                .flatMapMany(Flux::fromIterable)
                .map(productCache -> new Product(
                        productCache.id(),
                        productCache.title(),
                        productCache.description(),
                        productCache.imageName(),
                        null,
                        productCache.price()
                ))
                .flatMap(this::cacheObject);
    }

    private String getTotalCountKey(String search) {
        String key = "total_count";
        if (search.isBlank()) {
            key = "total_count:empty";
        } else {
            key = key + ":" + search;
        }

        return key;
    }

    @Override
    public Mono<Integer> cacheTotalCount(Integer totalCount, String search) {
        return redisTotalCountTemplate
                .opsForValue()
                .set(getTotalCountKey(search), totalCount, Duration.ofSeconds(TTL))
                .thenReturn(totalCount);
    }

    @Override
    public Mono<Integer> getCachedTotalCount(String search) {
        return redisTotalCountTemplate
                .opsForValue()
                .getAndExpire(getTotalCountKey(search), Duration.ofSeconds(TTL));
    }
}
