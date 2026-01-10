package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.bookProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.briefcaseProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.vaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class CartItemServiceImplTest {
    @Autowired
    protected CartItemService cartItemService;
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
    public void test_changeProductCountInTheCart_cartItemCached() {
        CartItem cached = cartItem(1L, 3).get();
        redisDataManager.cacheCartItem(cached);

        StepVerifier.create(r2dbcEntityTemplate
                .select(CartItem.class)
                .matching(query(where("product_id").is(1L)))
                .one())
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.DELETE))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
    }

    @Test
    public void test_findAllWithRelations_productsNotCached() {
        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(cartItemService.findAllWithRelations())
                .assertNext(cartItem -> {
                    CartItem expected = cartItem(1L, 3).withProduct(bookProduct().get()).get();
                    assertEquals(expected, cartItem);
                })
                .assertNext(cartItem -> {
                    CartItem expected = cartItem(2L, 1).withProduct(briefcaseProduct().get()).get();
                    assertEquals(expected, cartItem);
                })
                .assertNext(cartItem -> {
                    CartItem expected = cartItem(5L, 2).withProduct(vaseProduct().get()).get();
                    assertEquals(expected, cartItem);
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_removeAll_cartItemCached() {
        CartItem cachedCartItem1 = cartItem(1L, 3).get();
        CartItem cachedCartItem2 = cartItem(2L, 1).get();
        CartItem cachedCartItem3 = cartItem(4L, 2).get();

        redisDataManager.cacheCartItem(cachedCartItem1);
        redisDataManager.cacheCartItem(cachedCartItem2);
        redisDataManager.cacheCartItem(cachedCartItem3);

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all())
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(cartItemService.removeAll())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
    }
}
