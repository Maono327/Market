package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.exceptions.InsufficientFundsException;
import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.services.OrderService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.bookProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.polaroidProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.umbrellaProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.vaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class OrderServiceImplTest {
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;
    @Autowired
    private RedisDataManager redisDataManager;

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start(8090);
    }

    @AfterEach
    void clear() throws IOException {
        server.shutdown();
    }

    @Test
    public void test_findAllWithRelations() {
        StepVerifier.create(orderService.findAllWithRelations())
                .assertNext(order -> {
                    Order expected = order(1L).withOrderItems()
                            .orderItem(bookProduct().get(), 2)
                            .orderItem(umbrellaProduct().get(), 1)
                            .getItems().get();
                    assertEquals(expected, order);
                })
                .assertNext(order -> {
                    Order expected = order(2L).withOrderItems()
                            .orderItem(bookProduct().get(), 3)
                            .orderItem(polaroidProduct().get(), 1)
                            .orderItem(umbrellaProduct().get(), 1)
                            .orderItem(vaseProduct().get(), 2)
                            .getItems().get();
                    assertEquals(expected, order);
                })
                .verifyComplete();
    }

    @Test
    public void test_findByIdWithRelations() {
        StepVerifier.create(orderService.findByIdWithRelations(1L))
                .assertNext(order -> {
                    Order expected = order(1L).withOrderItems()
                            .orderItem(bookProduct().get(), 2)
                            .orderItem(umbrellaProduct().get(), 1)
                            .getItems().get();
                    assertEquals(expected, order);
                })
                .verifyComplete();

        StepVerifier.create(orderService.findByIdWithRelations(2L))
                .assertNext(order -> {
                    Order expected = order(2L).withOrderItems()
                            .orderItem(bookProduct().get(), 3)
                            .orderItem(polaroidProduct().get(), 1)
                            .orderItem(umbrellaProduct().get(), 1)
                            .orderItem(vaseProduct().get(), 2)
                            .getItems().get();
                    assertEquals(expected, order);
                })
                .verifyComplete();
    }

    @Test
    public void test_buy_productsCached_doPayment_ok() {
        server.enqueue(new MockResponse().setResponseCode(200));

        Product cachedBook = bookProduct().get();
        Product cachedPolaroid = polaroidProduct().get();
        Product cachedVase = vaseProduct().get();

        redisDataManager.cacheProduct(cachedBook);
        redisDataManager.cacheProduct(cachedPolaroid);
        redisDataManager.cacheProduct(cachedVase);

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all()).expectNextCount(3).verifyComplete();
        StepVerifier.create(r2dbcEntityTemplate
                .select(Order.class)
                .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0).verifyComplete();

        StepVerifier.create(orderService.buy())
                        .assertNext(order -> {
                            Order expected = order(3L, BigDecimal.valueOf(32099.95)).get();
                            assertEquals(expected, order);
                        }).verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all()).expectNextCount(0).verifyComplete();
        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                        .expectNextCount(1).verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                        .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                        .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();

        redisDataManager.clear();
        resetDataManager.resetAll();
    }

    @Test
    public void test_buy_doPayment_409() {
        server.enqueue(new MockResponse().setResponseCode(409));

        Product cachedBook = bookProduct().get();
        Product cachedPolaroid = polaroidProduct().get();
        Product cachedVase = vaseProduct().get();

        redisDataManager.cacheProduct(cachedBook);
        redisDataManager.cacheProduct(cachedPolaroid);
        redisDataManager.cacheProduct(cachedVase);

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all()).expectNextCount(3).verifyComplete();
        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0).verifyComplete();

        StepVerifier.create(orderService.buy())
                .expectError(InsufficientFundsException.class)
                .verify();

        StepVerifier.create(r2dbcEntityTemplate.select(CartItem.class).all()).expectNextCount(3).verifyComplete();
        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(3, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                .verifyComplete();

        redisDataManager.clear();
        resetDataManager.resetAll();
    }
}
