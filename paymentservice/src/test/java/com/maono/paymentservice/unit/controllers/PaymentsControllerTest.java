package com.maono.paymentservice.unit.controllers;

import com.maono.paymentservice.controllers.PaymentsController;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.services.impl.BalanceOperationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PaymentsController.class)
@AutoConfigureWebTestClient
public class PaymentsControllerTest {

    @Autowired
    protected WebTestClient webTestClient;
    @MockitoBean
    protected BalanceOperationServiceImpl balanceOperationService;

    @Test
    void test_balance_ok() {
        AccountBalance balanceFromDb = new AccountBalance(new BigDecimal("250.10"));
        when(balanceOperationService.getBalance()).thenReturn(Mono.just(balanceFromDb));

        webTestClient.get()
                .uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(BigDecimal.class)
                .value(balance -> assertEquals(new BigDecimal("250.10"), balance));

        verify(balanceOperationService).getBalance();
        verifyNoMoreInteractions(balanceOperationService);
    }

    @Test
    void test_balance_404() {
        when(balanceOperationService.getBalance()).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(balanceOperationService).getBalance();
        verifyNoMoreInteractions(balanceOperationService);
    }

    @Test
    void test_buy_ok() {
        AccountBalance balance = new AccountBalance(new BigDecimal("100"));
        when(balanceOperationService.doPayment(eq(new BigDecimal("50")))).thenReturn(Mono.just(balance));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "50")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(balanceOperationService).doPayment(eq(new BigDecimal("50")));
        verifyNoMoreInteractions(balanceOperationService);
    }
}
