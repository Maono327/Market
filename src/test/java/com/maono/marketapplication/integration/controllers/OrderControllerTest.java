package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(IntegrationTestConfiguration.class)
public class OrderControllerTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_getOrders() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<a href=\"/orders/1\">Заказ №1</a>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Книга (2 шт.) 1399.98 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Зонтик (1 шт.) 1999.99 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 3399.97 руб.</b>"));

                    assertTrue(html.contains("<a href=\"/orders/2\">Заказ №2</a>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Книга (3 шт.) 2099.97 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Polaroid (1 шт.) 6599.99 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Зонтик (1 шт.) 1999.99 руб.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">Ваза (2 шт.) 25999.98 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 36699.93 руб.</b>"));
                });
    }

    @Test
    public void test_findOrder_newOrder() {
        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Поздравляем! Успешная покупка!"));

                    assertTrue(html.contains("<h2>Заказ №1</h2>"));

                    assertTrue(html.contains("<b>Книга</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">2 шт.</li>"));
                    assertTrue(html.contains(" <li class=\"list-group-item\">699.99 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 1399.98 руб.</b>"));

                    assertTrue(html.contains("<b>Зонтик</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1999.99 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 1999.99 руб.</b>"));

                    assertTrue(html.contains("<h3>Сумма: 3399.97 руб.</h3>"));
                });

        resetDataManager.resetAll();
    }

    @Test
    public void test_findOrder() {
        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("Поздравляем! Успешная покупка!"));

                    assertTrue(html.contains("<h2>Заказ №1</h2>"));

                    assertTrue(html.contains("<b>Книга</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">2 шт.</li>"));
                    assertTrue(html.contains(" <li class=\"list-group-item\">699.99 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 1399.98 руб.</b>"));

                    assertTrue(html.contains("<b>Зонтик</b>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1 шт.</li>"));
                    assertTrue(html.contains("<li class=\"list-group-item\">1999.99 руб.</li>"));
                    assertTrue(html.contains("<b>Сумма: 1999.99 руб.</b>"));

                    assertTrue(html.contains("<h3>Сумма: 3399.97 руб.</h3>"));
                });
    }
}
