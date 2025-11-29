package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.CartController;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.util.ProductActionType;
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
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItemList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {CartController.class})
@AutoConfigureWebTestClient
class CartControllerTest {
    @MockitoBean
    protected CartItemService cartItemService;
    @Autowired
    protected WebTestClient webTestClient;

    @Test
    public void test_getCartItems() {
        when(cartItemService.findAllWithRelations()).thenReturn(Flux.fromIterable(cartItemList(List.of(2, 3, 5, 3, 2))));
        when(cartItemService.calculateTotalSum(anyList()))
                .thenReturn(BigDecimal.valueOf(2 * 101 + 3 * 102 + 5 * 104 + 3 * 105 + 2 * 106));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct1</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct2</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct3</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct4</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct5</h5>"));
                    assertTrue(html.contains("<h2>Итого: 1555 руб.</h2>" ));
                });

        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verifyNoMoreInteractions(cartItemService);
    }

    @Test
    public void test_changeProductCountInTheCart() {
        when(cartItemService.changeProductCountInTheCart(anyLong(), any(ProductActionType.class)))
                .thenReturn(Mono.empty());
        when(cartItemService.findAllWithRelations()).thenReturn(Flux.fromIterable(cartItemList(List.of(2, 3, 4, 3, 2))));
        when(cartItemService.calculateTotalSum(anyList()))
                .thenReturn(BigDecimal.valueOf(2 * 101 + 3 * 102 + 4 * 104 + 3 * 105 + 2 * 106));

        webTestClient.post()
                .uri("/cart/items?id=1&action=MINUS")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct1</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct2</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct3</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct4</h5>"));
                    assertTrue(html.contains("<h5 class=\"card-title\">TitleProduct5</h5>"));
                    assertTrue(html.contains("<h2>Итого: 1451 руб.</h2>"));
                });

        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verify(cartItemService).changeProductCountInTheCart(1L, ProductActionType.MINUS);
        verifyNoMoreInteractions(cartItemService);
    }
}