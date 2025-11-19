package com.maono.marketapplication.integration.repositories.Implementations;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.repositories.implemantations.OrderRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.bookProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.polaroidProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.umbrellaProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.vaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@Import({IntegrationTestConfiguration.class, OrderRepositoryImpl.class})
public class OrderRepositoryImplTest {

    @Autowired
    protected OrderRepositoryImpl orderRepository;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_findAllWithRelations() {
        Flux<Order> orderFlux = orderRepository.findAllWithRelations();
        StepVerifier.create(orderFlux)
                .assertNext(order -> {
                    Order expected = expectedOrderById(1L);
                    assertEquals(expected.getId(), order.getId());
                    assertEquals(expected.getTotalSum(), order.getTotalSum());
                    assertEquals(expected.getItems(), order.getItems());
                })
                .assertNext(order -> {
                    Order expected = expectedOrderById(2L);
                    assertEquals(expected.getId(), order.getId());
                    assertEquals(expected.getTotalSum(), order.getTotalSum());
                    assertEquals(expected.getItems(), order.getItems());
                })
                .verifyComplete();
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L})
    @ParameterizedTest
    public void test_findByIdWithRelations(Long id) {
        Order expected = expectedOrderById(id);

        if (expected != null) {
            StepVerifier.create(orderRepository.findByIdWithRelations(id))
                    .assertNext(order -> {
                        assertEquals(expected.getId(), order.getId());
                        assertEquals(expected.getTotalSum(), order.getTotalSum());
                        assertEquals(expected.getItems(), order.getItems());
                    }).verifyComplete();
        } else {
            StepVerifier.create(orderRepository.findByIdWithRelations(id))
                    .expectNextCount(0)
                    .verifyComplete();
        }
    }

    protected Order expectedOrderById(Long id) {
        if (id == 1L) {
            return order(1L)
                    .withOrderItems()
                    .orderItem(bookProduct().get(), 2)
                    .orderItem(umbrellaProduct().get(), 1)
                    .getItems().get();
        } else if (id == 2L) {
            return order(2L)
                    .withOrderItems()
                    .orderItem(bookProduct().get(), 3)
                    .orderItem(polaroidProduct().get(), 1)
                    .orderItem(umbrellaProduct().get(), 1)
                    .orderItem(vaseProduct().get(), 2)
                    .getItems().get();
        } else {
            return null;
        }
    }

    @Test
    public void test_save() {
        Order order = order(null, BigDecimal.valueOf(3000)).get();
        Order expected = order(3L, BigDecimal.valueOf(3000)).get();

        StepVerifier.create(orderRepository.save(order))
                .assertNext(saved -> {
                    assertEquals(saved.getId(), expected.getId());
                    assertEquals(saved.getTotalSum(), expected.getTotalSum());
                })
                .verifyComplete();

        resetDataManager.resetOrders();
    }
}
