package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(IntegrationTestConfiguration.class)
public class ProductControllerTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected ResetDataManager resetDataManager;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;

    @Test
    public void test_getProducts_emptySearch() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">699.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Интересная книга</p>"));
                    assertTrue(html.contains("<span>3</span>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">4000.00 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Удобный и красивый портфель</p>"));
                    assertTrue(html.contains("<span>1</span>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Polaroid</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">6599.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Для крутых фотографий</p>"));
                    assertTrue(html.contains("<span>0</span>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Зонтик</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">1999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Красивый и прозрачный зонт</p>"));
                    assertTrue(html.contains("<span>0</span>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">12999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Дизайнерская ваза</p>"));
                    assertTrue(html.contains("<span>2</span>"));

                    assertTrue(html.contains("<span>Страница: 1</span>"));
                });
    }

    @Test
    public void test_getProducts_search() {
        webTestClient.get()
                .uri("/items?search=о")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("<h5 class=\"card-title\">Книга</h5>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Портфель</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">4000.00 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Удобный и красивый портфель</p>"));
                    assertTrue(html.contains("<span>1</span>"));

                    assertFalse(html.contains("<h5 class=\"card-title\">Polaroid</h5>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Зонтик</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">1999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Красивый и прозрачный зонт</p>"));
                    assertTrue(html.contains("<span>0</span>"));

                    assertFalse(html.contains("<h5 class=\"card-title\">Ваза</h5>"));

                    assertTrue(html.contains("<span>Страница: 1</span>"));
                });
    }

    @Test
    public void test_getProducts_mixed() {
        webTestClient.get()
                .uri("/items?pageNumber=2&pageSize=2&sort=ALPHA")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("<h5 class=\"card-title\">Polaroid</h5>"));

                    assertFalse(html.contains("<h5 class=\"card-title\">Ваза</h5>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Зонтик</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">1999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Красивый и прозрачный зонт</p>"));
                    assertTrue(html.contains("<span>0</span>"));

                    assertTrue(html.contains("<h5 class=\"card-title\">Книга</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">699.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Интересная книга</p>"));
                    assertTrue(html.contains("<span>3</span>"));

                    assertFalse(html.contains("<h5 class=\"card-title\">Портфель</h5>"));

                    assertTrue(html.contains("<span>Страница: 2</span>"));
                });
    }

    @Test
    public void test_changeProductCountInTheCart_PLUS() {
        StepVerifier.create(r2dbcEntityTemplate
                .select(CartItem.class)
                .matching(query(where("id").is(1L)))
                .one())
                .assertNext(cartItem -> assertEquals(3L, cartItem.getCount()))
                .verifyComplete();

        webTestClient.post()
                .uri("/items?id=1&action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=&sort=NO&pageNumber=1&pageSize=5");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(4L, cartItem.getCount()))
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_changeProductCountInTheCart_MINUS() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3L, cartItem.getCount()))
                .verifyComplete();

        webTestClient.post()
                .uri("/items?id=1&action=MINUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=&sort=NO&pageNumber=1&pageSize=5");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2L, cartItem.getCount()))
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_changeProductCountInTheCart_PLUS_with_params() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3L, cartItem.getCount()))
                .verifyComplete();

        webTestClient.post()
                .uri("/items?id=1&pageSize=2&pageNumber=1&action=PLUS")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=&sort=NO&pageNumber=1&pageSize=2");

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(1L)))
                        .one())
                .assertNext(cartItem -> assertEquals(4L, cartItem.getCount()))
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_getProduct() {
        webTestClient.get()
                .uri("/items/{id}", 5L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">12999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Дизайнерская ваза</p>"));
                    assertTrue(html.contains("<span>2</span>"));
                });
    }

    @Test
    public void test_changeProductCountInTheCartOnProductPage_PLUS() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(5L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2L, cartItem.getCount()))
                .verifyComplete();

        webTestClient.post()
                .uri("/items/{id}", 5L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">12999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Дизайнерская ваза</p>"));
                    assertTrue(html.contains("<span>3</span>"));
                });

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(5L)))
                        .one())
                .assertNext(cartItem -> assertEquals(3L, cartItem.getCount()))
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_changeProductCountInTheCartOnProductPage_MINUS() {
        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(5L)))
                        .one())
                .assertNext(cartItem -> assertEquals(2L, cartItem.getCount()))
                .verifyComplete();

        webTestClient.post()
                .uri("/items/{id}", 5L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "MINUS"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">Ваза</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">12999.99 руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">Дизайнерская ваза</p>"));
                    assertTrue(html.contains("<span>1</span>"));
                });

        StepVerifier.create(r2dbcEntityTemplate
                        .select(CartItem.class)
                        .matching(query(where("id").is(5L)))
                        .one())
                .assertNext(cartItem -> assertEquals(1L, cartItem.getCount()))
                .verifyComplete();

        resetDataManager.resetCartItems();
    }
}

