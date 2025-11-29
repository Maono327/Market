package com.maono.marketapplication.integration.services.util;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class ProductActionStrategyTest {
    @Autowired
    protected ProductActionStrategy actionStrategy;
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;

    @AfterEach
    public void reset() {
        resetDataManager.resetCartItems();
    }

    @Test
    public void test_execute_incrementAction() {
        StepVerifier.create(r2dbcEntityTemplate
                .select(CartItem.class)
                .matching(query(where("product_id").is(1L)))
                .one())
                .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(actionStrategy.execute(ProductActionType.PLUS, 1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(4, cartItem.getCount()))
                .verifyComplete();
    }

    @Test
    public void test_execute_decrementAction() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3, cartItem.getCount()))
                .verifyComplete();

        StepVerifier.create(actionStrategy.execute(ProductActionType.MINUS, 1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2, cartItem.getCount()))
                .verifyComplete();
    }

    @Test
    public void test_execute_deleteAction() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(actionStrategy.execute(ProductActionType.DELETE, 1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(1L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();
    }
}
