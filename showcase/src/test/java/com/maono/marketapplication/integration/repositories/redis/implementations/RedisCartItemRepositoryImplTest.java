package com.maono.marketapplication.integration.repositories.redis.implementations;

import com.maono.marketapplication.integration.IntegrationTestRedisConfiguration;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.redis.implementations.RedisCartItemRepositoryImpl;
import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataRedisTest
@Import({
        IntegrationTestRedisConfiguration.class,
        RedisCartItemRepositoryImpl.class
})
public class RedisCartItemRepositoryImplTest {

    @Autowired
    protected RedisCartItemRepositoryImpl redisCartItemRepository;
    @Autowired
    protected ReactiveRedisTemplate<String, CartItemCache> redisTemplate;

    @BeforeEach
    public void clean() {
        redisTemplate.execute(connection -> connection.serverCommands()
                .flushAll())
                .then()
                .block();
    }

    @Test
    public void test_cacheObject() {
        CartItem cartItem = cartItem(2L, 3).get();

        CartItem expected = cartItem(2L, 3).get();
        StepVerifier.create(redisCartItemRepository.cacheObject(cartItem))
                .assertNext(cached -> {
                    assertEquals(expected, cached);
                }).verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get("cartitem:2"))
                .assertNext(cartItemCache -> {
                    CartItemCache cacheExpected = new CartItemCache(2L, 3);
                    assertEquals(cacheExpected, cartItemCache);
                })
                .verifyComplete();

        verifyTtl(2L);
    }

    @Test
    public void test_dropAllCounts() {
        List<CartItem> cached = List.of(
                cartItem(1L, 4).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 6).get(),
                cartItem(5L,1).get(),
                cartItem(7L, 0).get()
        );

        for (CartItem c : cached) {
            redisTemplate.opsForValue().set(
                    "cartitem:" + c.getId(),
                    new CartItemCache(c.getId(), c.getCount()),
                    Duration.ofSeconds(120)).block();
        }

        StepVerifier.create(redisCartItemRepository.dropAllCounts())
                .expectNextCount(0)
                .verifyComplete();

        for (CartItem c : cached) {
            StepVerifier.create(redisTemplate.opsForValue().get("cartitem:" + c.getId()))
                    .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                    .verifyComplete();
            verifyTtl(c.getId());
        }
    }

    @Test
    public void test_getCachedObject() {
        redisTemplate.opsForValue().set(
                "cartitem:2",
                new CartItemCache(2L, 3),
                Duration.ofSeconds(120))
                .block();

        CartItem expected = cartItem(2L, 3).get();

        StepVerifier.create(redisCartItemRepository.getCachedObject(2L))
                .assertNext(cartItem -> assertEquals(expected, cartItem))
                .verifyComplete();

        verifyTtl(2L);
    }

    @Test
    public void test_multiGet() {
        List<CartItem> cached = List.of(
                cartItem(1L, 4).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 6).get(),
                cartItem(5L,1).get(),
                cartItem(7L, 0).get()
        );

        for (CartItem c : cached) {
            redisTemplate.opsForValue().set(
                    "cartitem:" + c.getId(),
                    new CartItemCache(c.getId(), c.getCount()),
                    Duration.ofSeconds(120))
                    .block();
        }

        StepVerifier.create(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)))
                .assertNext(cartItem -> assertEquals(cartItem(1L, 4).get(), cartItem))
                .assertNext(cartItem -> assertEquals(cartItem(2L, 2).get(), cartItem))
                .assertNext(cartItem -> assertEquals(cartItem(3L, 6).get(), cartItem))
                .assertNext(cartItem -> assertEquals(cartItem(5L, 1).get(), cartItem))
                .assertNext(cartItem -> assertEquals(cartItem(7L, 0).get(), cartItem))
                .verifyComplete();

        for (CartItem c : cached) {
            verifyTtl(c.getId());
        }
    }

    protected void verifyTtl(Long id) {
        StepVerifier.create(redisTemplate.getExpire("cartitem:" + id))
                .assertNext(ttl -> {
                    assertTrue(ttl.getSeconds() > 0);
                    assertTrue(ttl.getSeconds() <= 120);
                })
                .verifyComplete();
    }
}
