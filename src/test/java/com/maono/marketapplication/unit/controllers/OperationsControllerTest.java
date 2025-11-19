package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.OperationsController;
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
    public void test_createOrder() {
        when(orderService.buy()).thenReturn(Mono.just(order(1L, BigDecimal.valueOf(12399)).get()));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }
}