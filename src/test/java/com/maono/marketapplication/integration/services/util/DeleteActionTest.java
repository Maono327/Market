package com.maono.marketapplication.integration.services.util;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.services.util.DeleteAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class DeleteActionTest {
    @Autowired
    protected DeleteAction deleteAction;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_executeChange() {
        StepVerifier.create(r2dbcEntityTemplate
                .select(CartItem.class)
                .matching(query(where("product_id").is(5L)))
                .one())
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(deleteAction.executeChange(5L))
                        .expectNextCount(0)
                        .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("product_id").is(5L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        resetDataManager.resetCartItems();
    }
}
