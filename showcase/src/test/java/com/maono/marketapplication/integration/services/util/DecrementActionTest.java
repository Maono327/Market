package com.maono.marketapplication.integration.services.util;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import com.maono.marketapplication.services.util.DecrementAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class DecrementActionTest {
    @Autowired
    protected DecrementAction decrementAction;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;
    @Autowired
    protected RedisDataManager redisDataManager;
    @Autowired
    protected ReactiveRedisTemplate<String, CartItemCache> redisTemplate;

    @AfterEach
    public void setup() {
        redisDataManager.clear();
        resetDataManager.resetCartItems();
    }

    @Test
    public void test_executeChange_decrement_cartItemCached() {
        CartItem cached = cartItem(1L, 3).get();
        redisDataManager.cacheCartItem(cached);
        assertNotNull(redisTemplate.opsForValue().get("cartitem:1").block());

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                        .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                        .verifyComplete();

        StepVerifier.create(decrementAction.executeChange(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                        .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                        .verifyComplete();
    }

    @Test
    public void test_executeChange_decrement_cartItemNotCached() {
        assertNull(redisTemplate.opsForValue().get("cartitem:1").block());

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(decrementAction.executeChange(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                .verifyComplete();
    }

    @Test
    public void test_executeChange_delete_cartItemCached() {
        CartItem cached = cartItem(2L, 1).get();
        redisDataManager.cacheCartItem(cached);
        assertNotNull(redisTemplate.opsForValue().get("cartitem:2").block());

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .assertNext(cartItem -> assertEquals(1, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(decrementAction.executeChange(2L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get("cartitem:2"))
                        .assertNext(cartItemCache ->  assertEquals(0, cartItemCache.count()))
                        .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_executeChange_delete_cartItemNotCached() {
        assertNull(redisTemplate.opsForValue().get("cartitem:2").block());

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .assertNext(cartItem -> assertEquals(1, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(decrementAction.executeChange(2L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get("cartitem:2"))
                .assertNext(cartItemCache ->  assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_executeChange_delete_cartItemNotExist() {
        r2dbcEntityTemplate.delete(cartItem(2L, 1).get()).block();

        assertNull(redisTemplate.opsForValue().get("cartitem:2").block());

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(decrementAction.executeChange(2L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get("cartitem:2"))
                .assertNext(cartItemCache ->  assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(2L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();
    }
}
