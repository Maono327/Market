package com.maono.paymentservice.unit.exceptions;

import com.maono.paymentservice.controllers.PaymentsController;
import com.maono.paymentservice.exceptions.BalanceNotFoundException;
import com.maono.paymentservice.exceptions.InsufficientFundsException;
import com.maono.paymentservice.services.impl.BalanceOperationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PaymentsController.class)
@AutoConfigureWebTestClient
public class GlobalExceptionHandlerTest {

    @Autowired
    protected WebTestClient webTestClient;
    @MockitoBean
    protected BalanceOperationServiceImpl balanceOperationService;

    @Test
    void test_buy_404() {
        when(balanceOperationService.doPayment(eq(new BigDecimal("50"))))
                .thenReturn(Mono.error(new BalanceNotFoundException()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "50")
                        .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        verify(balanceOperationService).doPayment(eq(new BigDecimal("50")));
        verifyNoMoreInteractions(balanceOperationService);
    }

    @Test
    void test_buy_400() {
        when(balanceOperationService.doPayment(eq(new BigDecimal("-1"))))
                .thenReturn(Mono.error(new IllegalArgumentException()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "-1")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();

        verify(balanceOperationService).doPayment(eq(new BigDecimal("-1")));
        verifyNoMoreInteractions(balanceOperationService);
    }

    @Test
    void test_buy_409() {
        when(balanceOperationService.doPayment(eq(new BigDecimal("50"))))
                .thenReturn(Mono.error(new InsufficientFundsException()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/buy")
                        .queryParam("sum", "50")
                        .build())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().isEmpty();

        verify(balanceOperationService).doPayment(eq(new BigDecimal("50")));
        verifyNoMoreInteractions(balanceOperationService);
    }
}
