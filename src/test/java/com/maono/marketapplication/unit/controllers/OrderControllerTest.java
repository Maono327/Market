package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.OrderController;
import com.maono.marketapplication.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = OrderController.class)
@AutoConfigureWebTestClient
class OrderControllerTest {
    @MockitoBean
    protected OrderService orderService;
    @Autowired
    protected WebTestClient webTestClient;

    @Test
    public void test_getOrders() {
        when(orderService.findAllWithRelations()).thenReturn(Flux.just(
                order(1L, BigDecimal.valueOf(2*101 + 4*102 + 104))
                        .withOrderItems()
                        .orderItem(1L, 2)
                        .orderItem(2L, 4)
                        .orderItem(4L, 1)
                        .getItems()
                        .get(),

                order(2L, BigDecimal.valueOf(101 + 2*103))
                        .withOrderItems()
                        .orderItem(1L, 1)
                        .orderItem(3L, 2)
                        .getItems()
                        .get(),

                order(3L, BigDecimal.valueOf(105))
                        .withOrderItems()
                        .orderItem(5L, 1)
                        .getItems()
                        .get()
        ));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<a href=\"/orders/1\">Заказ №1</a>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct1 (2 шт.) 202 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct2 (4 шт.) 408 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct4 (1 шт.) 104 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 714 руб.</b>"));

                    assertTrue(html.contains("<a href=\"/orders/2\">Заказ №2</a>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct1 (1 шт.) 101 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct3 (2 шт.) 206 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 307 руб.</b>"));

                    assertTrue(html.contains("<a href=\"/orders/3\">Заказ №3</a>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">TitleProduct5 (1 шт.) 105 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 105 руб.</b>"));
                });

        verify(orderService).findAllWithRelations();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_findOrder_newOrder() {
        when(orderService.findByIdWithRelations(1L)).thenReturn(Mono.just(
                order(1L, BigDecimal.valueOf(3*101 + 103 + 2*104 + 105))
                        .withOrderItems()
                        .orderItem(1L, 3)
                        .orderItem(3L, 1)
                        .orderItem(4L, 2)
                        .orderItem(5L, 1)
                        .getItems().get()
        ));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Поздравляем! Успешная покупка!"));

                    assertTrue(html.contains("<h2>Заказ №1</h2>"));

                    assertTrue(html.contains("<b>TitleProduct1</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">3 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">101 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 303 руб.</b>"));

                    assertTrue(html.contains("<b>TitleProduct3</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">103 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 103 руб.</b>"));

                    assertTrue(html.contains("<b>TitleProduct4</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">2 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">104 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 208 руб.</b>"));

                    assertTrue(html.contains(" <b>TitleProduct5</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">105 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 105 руб.</b>"));


                    assertTrue(html.contains("<h3>Сумма: 719 руб.</h3>"));
                });

        verify(orderService).findByIdWithRelations(1L);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void test_findOrder() {
        when(orderService.findByIdWithRelations(1L)).thenReturn(Mono.just(
                order(1L, BigDecimal.valueOf(3*101 + 103 + 2*104 + 105))
                        .withOrderItems()
                        .orderItem(1L, 3)
                        .orderItem(3L, 1)
                        .orderItem(4L, 2)
                        .orderItem(5L, 1)
                        .getItems().get()
        ));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("Поздравляем! Успешная покупка!"));

                    assertTrue(html.contains("<h2>Заказ №1</h2>"));

                    assertTrue(html.contains("<b>TitleProduct1</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">3 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">101 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 303 руб.</b>"));

                    assertTrue(html.contains("<b>TitleProduct3</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">103 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 103 руб.</b>"));

                    assertTrue(html.contains("<b>TitleProduct4</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">2 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">104 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 208 руб.</b>"));

                    assertTrue(html.contains(" <b>TitleProduct5</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">105 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 105 руб.</b>"));


                    assertTrue(html.contains("<h3>Сумма: 719 руб.</h3>"));
                });

        verify(orderService).findByIdWithRelations(1L);
        verifyNoMoreInteractions(orderService);
    }
}