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
public class CartControllerTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_getCartItems() {
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
                });
    }

    @Test
    public void test_changeProductCountInTheCart() {
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
    }
}
