package com.maono.marketapplication.integration.services.util;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.services.util.IncrementAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class IncrementActionTest {
    @Autowired
    protected IncrementAction incrementAction;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;
    @Autowired
    protected RedisDataManager redisDataManager;

    @AfterEach
    public void cleanUp() {
        redisDataManager.clear();
        resetDataManager.resetCartItems();
    }

    @Test
    public void test_executeChange_incrementCount_cartItemCached() {
        CartItem cached = cartItem(1L, 3).get();
        redisDataManager.cacheCartItem(cached);

        StepVerifier.create(r2dbcEntityTemplate
                .select(CartItem.class)
                .matching(query(where("product_id").is(1L)))
                .one())
                .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(incrementAction.executeChange(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(4, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                            .assertNext(cartItemCache -> assertEquals(4, cartItemCache.count()))
                            .verifyComplete();
    }

    @Test
    public void test_executeChange_incrementCount_cartItemNotCached() {
        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                        .expectNextCount(0)
                        .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(incrementAction.executeChange(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(4, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(4, cartItemCache.count()))
                .verifyComplete();
    }

    @Test
    public void test_executeChange_newCartItem() {
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(4L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(incrementAction.executeChange(4L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(4L)))
                        .one())
                .assertNext(cartItem -> assertEquals(1, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                        .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                        .verifyComplete();
    }

}
