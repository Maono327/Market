package com.maono.paymentservice.integration;

import com.maono.paymentservice.exceptions.BalanceNotFoundException;
import com.maono.paymentservice.exceptions.InsufficientFundsException;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.services.BalanceOperationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest(properties = "balance.filler.enabled=false")
@Import(PostgresqlContainerConfiguration.class)
public class BalanceOperationServiceImplTest {

    @Autowired
    protected BalanceOperationService balanceOperationService;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;

    @AfterEach
    void cleanUp() {
        String SQL = "TRUNCATE TABLE balance RESTART IDENTITY;";

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }

    @Test
    void test_saveBalance() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(AccountBalance.class)
                        .matching(query(where("id")
                                .is(1L)))
                        .first())
                .expectNextCount(0)
                .verifyComplete();

        AccountBalance balance = new AccountBalance(new BigDecimal("250.10"));
        AccountBalance expected = new AccountBalance(1L, new BigDecimal("250.10"));

        StepVerifier.create(balanceOperationService.saveBalance(balance))
                .assertNext(saved -> assertEquals(expected, saved))
                .verifyComplete();
    }

    @Test
    void test_getBalance() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("250.10"));

        StepVerifier.create(balanceOperationService.getBalance())
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();

    }

    @Test
    void test_doPayment() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("150.10"));

        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("100")))
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();
    }

    @Test
    void test_doPayment_throwIllegalArgumentException() {
        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("-1")))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void test_doPayment_throwBalanceNotFoundException() {
        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("100")))
                .expectError(BalanceNotFoundException.class)
                .verify();
    }

    @Test
    void test_doPayment_throwInsufficientFundsException() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();


        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("400")))
                .expectError(InsufficientFundsException.class)
                .verify();
    }
}
