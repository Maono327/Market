package com.maono.paymentservice.integration.repositories;

import com.maono.paymentservice.integration.PostgresqlContainerConfiguration;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.repositories.BalanceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@Import(PostgresqlContainerConfiguration.class)
public class BalanceRepositoryTest {
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    protected BalanceRepository balanceRepository;

    @AfterEach
    void cleanUp() {
        String SQL = "TRUNCATE TABLE balance RESTART IDENTITY;";

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }

    @Test
    public void test_save() {
        AccountBalance expected = new AccountBalance(1L, new BigDecimal("100"));

        StepVerifier.create(balanceRepository.save(new AccountBalance(new BigDecimal("100"))))
                .assertNext(saved -> assertEquals(expected, saved))
                .verifyComplete();
    }

    @Test
    public void test_update() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("50"));

        StepVerifier.create(balanceRepository.save(new AccountBalance(1L, new BigDecimal("50"))))
                .assertNext(updated -> assertEquals(expected, updated))
                .verifyComplete();
    }

    @Test
    public void test_findById() {
        r2dbcEntityTemplate
                .insert(AccountBalance.class)
                .into("balance")
                .using(new AccountBalance(new BigDecimal("250.10")))
                .block();

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("250.10"));

        StepVerifier.create(balanceRepository.findById(1L))
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();
    }
}
