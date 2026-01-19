package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.OperationsController;
import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.InsufficientFundsException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailabe;
import com.maono.marketapplication.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {OperationsController.class})
@AutoConfigureWebTestClient
class OperationsControllerTest {
    @MockitoBean
    protected OrderService orderService;
    @Autowired
    protected WebTestClient webTestClient;

    @Test
    public void test_createOrder_ok() {
        when(orderService.buy()).thenReturn(Mono.just(order(1L, BigDecimal.valueOf(12399)).get()));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_createOrder_404_balanceNotFound() {
        when(orderService.buy()).thenReturn(Mono.error(new BalanceNotFoundException("Баланс не найден")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=balanceNotFound");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_createOrder_409_insufficientFunds() {
        when(orderService.buy()).thenReturn(Mono.error(new InsufficientFundsException("Недостаточно средств")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=insufficientFunds");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_createOrder_503_purchaseServiceUnavailable() {
        when(orderService.buy()).thenReturn(Mono.error(new PurchaseServiceUnavailabe("Сервис платежей не доступен")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=unavailable");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_createOrder_400_illegal() {
        when(orderService.buy()).thenReturn(Mono.error(new IllegalArgumentException("Некорректная сумма")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?error=illegal");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }
}