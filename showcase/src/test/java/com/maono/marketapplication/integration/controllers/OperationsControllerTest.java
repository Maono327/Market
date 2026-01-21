package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.Order;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(IntegrationTestConfiguration.class)
public class OperationsControllerTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected ResetDataManager resetDataManager;

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

    @Autowired
    protected RedisDataManager redisDataManager;

    @Test
    public void test_createOrder_ok() {
        server.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(r2dbcEntityTemplate
                .select(Order.class)
                .matching(query(where("id").is(3L)))
                .one())
                .expectNextCount(0)
                .verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/3?newOrder=true");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(1)
                .verifyComplete();

        resetDataManager.resetAll();
        redisDataManager.clear();
    }

    @Test
    public void test_createOrder_400_illegalArgumentException() {
        server.enqueue(new MockResponse().setResponseCode(400));

        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=illegal");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        resetDataManager.resetAll();
        redisDataManager.clear();
    }

    @Test
    public void test_createOrder_503_purchaseServiceUnavailabe() {
        server.enqueue(new MockResponse().setResponseCode(503));

        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=unavailable");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(Order.class)
                        .matching(query(where("id").is(3L)))
                        .one())
                .expectNextCount(0)
                .verifyComplete();

        resetDataManager.resetAll();
        redisDataManager.clear();
    }
}
