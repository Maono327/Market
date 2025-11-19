package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

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
    public void test_buy() {
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

        resetDataManager.resetAll();
    }
}
