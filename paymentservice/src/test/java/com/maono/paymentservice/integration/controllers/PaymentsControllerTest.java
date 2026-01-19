package com.maono.paymentservice.integration.controllers;

import com.maono.paymentservice.integration.PostgresqlContainerConfiguration;
import com.maono.paymentservice.model.AccountBalance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest(properties = "balance.filler.enabled=false")
@AutoConfigureWebTestClient
@Import(PostgresqlContainerConfiguration.class)
public class PaymentsControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;

    @AfterEach
    void cleanUp() {
        String SQL = "TRUNCATE TABLE balance RESTART IDENTITY;";

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }

    @Test
    void test_balance_ok() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        webTestClient.get()
                .uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(BigDecimal.class)
                .value(balance -> assertEquals(new BigDecimal("250.10"), balance));
    }

    @Test
    void test_balance_404() {
        webTestClient.get()
                .uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void test_buy_200() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "50")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("200.10"));

        StepVerifier.create(r2dbcEntityTemplate.select(AccountBalance.class)
                .matching(query(where("id").is("1"))).first())
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();
    }

    @Test
    void test_buy_404() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "50")
                        .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}
