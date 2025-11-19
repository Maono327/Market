package com.maono.marketapplication.integration.repositories.Implementations;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.repositories.implemantations.OrderItemRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@DataR2dbcTest
@Import({IntegrationTestConfiguration.class, OrderItemRepositoryImpl.class})
class OrderItemRepositoryImplTest {
    @Autowired
    protected OrderItemRepositoryImpl orderItemRepository;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_saveOrderItems() {

        Order newOrder = order(3L).withOrderItems()
                .orderItem(4L, 2)
                .orderItem(5L, 3)
                .getItems().get();

        StepVerifier.create(r2dbcEntityTemplate.insert(newOrder))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(orderItemRepository.saveOrderItems(newOrder.getItems()))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate.select(OrderItem.class)
                        .matching(query(where("order_id").is(3L))).all())
                .assertNext(orderItem -> {
                    assertEquals(4L, orderItem.getProductId());
                    assertEquals(2, orderItem.getCount());
                })
                .assertNext(orderItem -> {
                    assertEquals(5L, orderItem.getProductId());
                    assertEquals(3, orderItem.getCount());
                })
                .verifyComplete();

        resetDataManager.resetOrders();
    }

}