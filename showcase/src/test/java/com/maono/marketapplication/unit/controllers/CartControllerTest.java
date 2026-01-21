package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.CartController;
import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailable;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.implementations.PurchaseServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    protected PurchaseServiceImpl purchaseService;
    @MockitoBean
    protected CartItemService cartItemService;
    @Autowired
    protected WebTestClient webTestClient;

    @Test
    public void test_getCartItems_purchaseService_OK_sufficientBalance() {
        when(purchaseService.getBalance()).thenReturn(Mono.just(new BigDecimal("400000000")));
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
                    assertTrue(html.contains("<button class=\"btn btn-warning ms-auto\""));
                    assertTrue(html.contains("aria-disabled=\"false\">"));
                    assertTrue(html.contains("Купить"));
                    assertTrue(html.contains("</button>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));

                });

        verify(purchaseService).getBalance();
        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verifyNoMoreInteractions(cartItemService, purchaseService);
    }

    @Test
    public void test_getCartItems_purchaseService_OK_insufficientBalance() {
        when(purchaseService.getBalance()).thenReturn(Mono.just(new BigDecimal("0")));
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
                    assertTrue(html.contains("<button class=\"btn btn-warning ms-auto\""));
                    assertTrue(html.contains("aria-disabled=\"true\" disabled=\"disabled\">"));
                    assertTrue(html.contains("Купить"));
                    assertTrue(html.contains("</button>"));

                    assertTrue(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));

                });

        verify(purchaseService).getBalance();
        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verifyNoMoreInteractions(cartItemService, purchaseService);
    }

    @Test
    public void test_getCartItems_purchaseService_404_balanceNotFound() {
        when(purchaseService.getBalance()).thenReturn(Mono.error(new BalanceNotFoundException("Баланс не найден")));
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
                    assertTrue(html.contains("<button class=\"btn btn-warning ms-auto\""));
                    assertTrue(html.contains("aria-disabled=\"true\" disabled=\"disabled\">"));
                    assertTrue(html.contains("Купить"));
                    assertTrue(html.contains("</button>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertTrue(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));

                });

        verify(purchaseService).getBalance();
        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verifyNoMoreInteractions(cartItemService, purchaseService);
    }

    @Test
    public void test_getCartItems_purchaseService_503_balanceNotFound() {
        when(purchaseService.getBalance()).thenReturn(Mono.error(new PurchaseServiceUnavailable("Сервис платежей не доступен")));
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
                    assertTrue(html.contains("<button class=\"btn btn-warning ms-auto\""));
                    assertTrue(html.contains("aria-disabled=\"true\" disabled=\"disabled\">"));
                    assertTrue(html.contains("Купить"));
                    assertTrue(html.contains("</button>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertTrue(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));

                });

        verify(purchaseService).getBalance();
        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verifyNoMoreInteractions(cartItemService, purchaseService);
    }

    @Test
    public void test_changeProductCountInTheCart() {
        when(purchaseService.getBalance()).thenReturn(Mono.just(new BigDecimal("1000000000")));
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
                    assertTrue(html.contains("aria-disabled=\"false\">"));
                    assertTrue(html.contains("Купить"));
                    assertTrue(html.contains("</button>"));

                    assertFalse(html.contains("<span>Недостаточно средств для оформления заказа.</span>"));
                    assertFalse(html.contains("<span>Баланс не найден. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Сервис платежей не доступен. Попробуйте позже.</span>"));
                    assertFalse(html.contains("<span>Произошла ошибка при оформлении заказа.</span>"));
                    assertFalse(html.contains("<span>Некорректная сумма.</span>"));
                });

        verify(purchaseService).getBalance();
        verify(cartItemService).findAllWithRelations();
        verify(cartItemService).calculateTotalSum(anyList());
        verify(cartItemService).changeProductCountInTheCart(1L, ProductActionType.MINUS);
        verifyNoMoreInteractions(cartItemService, purchaseService);
    }
}