package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(IntegrationTestConfiguration.class)
public class CartControllerTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected ResetDataManager resetDataManager;
    @Autowired
    protected RedisDataManager redisDataManager;

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

    @Test
    public void test_getCartItems_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("9999999999"));


        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<h2>Итого: 32099.95 руб.</h2>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));
                });
    }

    @Test
    public void test_getCartItems_404_balanceNotFoundException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<h2>Итого: 32099.95 руб.</h2>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertTrue(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));
                });
    }

    @Test
    public void test_getCartItems_503_purchaseServiceUnavailabe() {
        server.enqueue(new MockResponse().setResponseCode(503));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<h2>Итого: 32099.95 руб.</h2>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertTrue(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));
                });
    }

    @Test
    public void test_changeProductCountInTheCart_ok() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("9999999999"));

        webTestClient.post()
                .uri("/cart/items?id=1&action=DELETE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<h2>Итого: 29999.98 руб.</h2>"));
                });

        resetDataManager.resetCartItems();
        redisDataManager.clear();
    }
}
