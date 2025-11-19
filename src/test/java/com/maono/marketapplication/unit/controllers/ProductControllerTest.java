package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.ProductController;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductActionType;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.page;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.stubProduct;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ProductController.class)
@AutoConfigureWebTestClient
class ProductControllerTest {
    @MockitoBean
    protected ProductService productService;
    @MockitoBean
    protected CartItemService cartItemService;
    @Autowired
    protected WebTestClient webTestClient;

    @MethodSource("arguments_test_getProducts")
    @ParameterizedTest
    public void test_getProducts(String search,
                                 String sort,
                                 String pageSize,
                                 String pageNumber,
                                 int expectedPageNumber,
                                 List<Product> expectedProducts,
                                 Page<Product> pageFromService) {

        Map<String, String> params = new HashMap<>();
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        if (pageSize != null) params.put("pageSize", pageSize);
        if (pageNumber != null) params.put("pageNumber", pageNumber);

        when(productService.findByPage(
                params.getOrDefault("search", ""),
                ProductSortType.valueOf(params.getOrDefault("sort", "NO")),
                Integer.parseInt(params.getOrDefault("pageSize", "5")),
                Integer.parseInt(params.getOrDefault("pageNumber", "1"))))
                .thenReturn(Mono.just(pageFromService));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/items");
        if (search != null) uriBuilder.queryParam("search", search);
        if (sort != null) uriBuilder.queryParam("sort", sort);
        if (pageSize != null) uriBuilder.queryParam("pageSize", pageSize);
        if (pageNumber != null) uriBuilder.queryParam("pageNumber", pageNumber);
        String uri = uriBuilder.build().toUriString();
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    for (Product expectedProduct : expectedProducts) {
                        assertTrue(html.contains("<h5 class=\"card-title\">" + expectedProduct.getTitle() + "</h5>"));
                        assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">" +
                                expectedProduct.getPrice().toString() + " руб.</span>"));
                        assertTrue(html.contains("<p class=\"card-text\">" + expectedProduct.getDescription()+ "</p>"));
                        assertTrue(html.contains("<span>Страница: " + expectedPageNumber + "</span>"));
                        if (expectedProduct.getCartItem() != null) {
                            assertTrue(html.contains("<span>" + expectedProduct.getCartItem().getCount() + "</span>"));
                        }
                    }
                });

        verify(productService).findByPage(
                params.getOrDefault("search", ""),
                ProductSortType.valueOf(params.getOrDefault("sort", "NO")),
                Integer.parseInt(params.getOrDefault("pageSize", "5")),
                Integer.parseInt(params.getOrDefault("pageNumber", "1")));
        verifyNoMoreInteractions(productService);
        verifyNoInteractions(cartItemService);
    }

    protected static Stream<Arguments> arguments_test_getProducts() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        1,
                        List.of(productByIdTemplate(1L).withCartItemByCount(2).get(),
                                productByIdTemplate(2L).get(),
                                productByIdTemplate(3L).withCartItemByCount(3).get(),
                                productByIdTemplate(4L).get(),
                                productByIdTemplate(5L).withCartItemByCount(1).get()),
                        page(List.of(
                                productByIdTemplate(1L).withCartItemByCount(2).get(),
                                productByIdTemplate(2L).get(),
                                productByIdTemplate(3L).withCartItemByCount(3).get(),
                                productByIdTemplate(4L).get(),
                                productByIdTemplate(5L).withCartItemByCount(1).get(),
                                stubProduct()),
                                5,
                                1,
                                false,
                                false,
                                1
                                )
                        ),
                Arguments.of(
                        null,
                        "ALPHA",
                        null,
                        null,
                        1,
                        List.of(
                                productByIdTemplate(3L).withTitle("A").withCartItemByCount(3).get(),
                                productByIdTemplate(4L).withTitle("B").get(),
                                productByIdTemplate(5L).withTitle("C").withCartItemByCount(1).get(),
                                productByIdTemplate(1L).withTitle("D").withCartItemByCount(2).get(),
                                productByIdTemplate(2L).withTitle("E").get()),
                        page(List.of(
                                productByIdTemplate(3L).withTitle("A").withCartItemByCount(3).get(),
                                productByIdTemplate(4L).withTitle("B").get(),
                                productByIdTemplate(5L).withTitle("C").withCartItemByCount(1).get(),
                                productByIdTemplate(1L).withTitle("D").withCartItemByCount(2).get(),
                                productByIdTemplate(2L).withTitle("E").get()),
                                5,
                                1,
                                false,
                                false,
                                1)
                ),
                Arguments.of(
                        null,
                        "PRICE",
                        null,
                        null,
                        1,
                        List.of(
                                productByIdTemplate(1L).withPrice(10).withCartItemByCount(2).get(),
                                productByIdTemplate(4L).withPrice(20).get(),
                                productByIdTemplate(2L).withPrice(30).get(),
                                productByIdTemplate(5L).withPrice(40).withCartItemByCount(1).get(),
                                productByIdTemplate(3L).withPrice(50).withCartItemByCount(3).get()),
                        page(List.of(
                                    productByIdTemplate(1L).withPrice(10).withCartItemByCount(2).get(),
                                    productByIdTemplate(4L).withPrice(20).get(),
                                    productByIdTemplate(2L).withPrice(30).get(),
                                    productByIdTemplate(5L).withPrice(40).withCartItemByCount(1).get(),
                                    productByIdTemplate(3L).withPrice(50).withCartItemByCount(3).get()
                                ),
                                5,
                                1,
                                false,
                                false,
                                1)
                ),
                Arguments.of(
                        "Bo",
                        "ALPHA",
                        "2",
                        "2",
                        2,
                        List.of(
                                productByIdTemplate(2L).withTitle("Boa").get(),
                                productByIdTemplate(5L).withTitle("Bob").withCartItemByCount(1).get()),
                        page(List.of(
                                        productByIdTemplate(2L).withTitle("Boa").get(),
                                        productByIdTemplate(5L).withTitle("Bob").withCartItemByCount(1).get()
                                ),
                                2,
                                2,
                                true,
                                true,
                                3)
                ),
                Arguments.of(
                        "Bo",
                        "PRICE",
                        "2",
                        "3",
                        2,
                        List.of(
                                productByIdTemplate(2L).withTitle("Boa").get(),
                                productByIdTemplate(5L).withTitle("Bob").withCartItemByCount(1).get()),
                        page(List.of(
                                        productByIdTemplate(2L).withTitle("Boa").get(),
                                        productByIdTemplate(5L).withTitle("Bob").withCartItemByCount(1).get()
                                ),
                                2,
                                2,
                                false,
                                true,
                                3)
                )
        );
    }

    @MethodSource("arguments_test_changeProductCountInTheCart")
    @ParameterizedTest
    public void test_changeProductCountInTheCart(String id,
                                                 String search,
                                                 String sort,
                                                 String pageSize,
                                                 String pageNumber,
                                                 String action) {
        when(cartItemService.changeProductCountInTheCart(Long.valueOf(id), ProductActionType.valueOf(action)))
                .thenReturn(Mono.empty());

        UriComponentsBuilder expectedRedirectionURL = UriComponentsBuilder.fromPath("/items");
        expectedRedirectionURL.queryParam("search", search == null ? "" : search);
        expectedRedirectionURL.queryParam("sort", sort == null ? "NO" : sort);
        expectedRedirectionURL.queryParam("pageNumber", pageNumber == null ? "1" : pageNumber);
        expectedRedirectionURL.queryParam("pageSize", pageSize == null ? "5" : pageSize);

        BodyInserters.FormInserter<String> inserters = BodyInserters
                .fromFormData("id", id)
                .with("action", action);
        if (search != null) inserters.with("search", search);
        if (sort != null) inserters.with("sort", sort);
        if (pageSize != null) inserters.with("pageSize", pageSize);
        if (pageNumber != null) inserters.with("pageNumber", pageNumber);
        webTestClient.post()
                        .uri("/items")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(inserters)
                        .exchange()
                        .expectStatus().is3xxRedirection()
                        .expectHeader().valueEquals("Location", expectedRedirectionURL.toUriString());

        verify(cartItemService).changeProductCountInTheCart(Long.valueOf(id), ProductActionType.valueOf(action));
        verifyNoMoreInteractions(cartItemService);
        verifyNoInteractions(productService);
    }

    protected static Stream<Arguments> arguments_test_changeProductCountInTheCart() {
        return Stream.of(
                Arguments.of("1", null, null, null, null, "PLUS"),
                Arguments.of("2", null, "ALPHA", null, null, "MINUS"),
                Arguments.of("3", null, null, "5", null, "PLUS"),
                Arguments.of("3", "Книга", null, null, null, "PLUS"),
                Arguments.of("3", "Книга", null, "5", null, "PLUS"),
                Arguments.of("3", "Книга", "PRICE", "5", "2", "PLUS")
        );
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L})
    @ParameterizedTest
    public void test_getProduct(Long id) throws Exception {
        Product p = productByIdTemplate(id).withCartItemByCount(1).get();
        when(productService.findProductByIdWithRelations(id)).thenReturn(Mono.just(p));

        webTestClient.get()
                .uri("/items/{id}", id)
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                        .value(html -> {

                            assertTrue(html.contains("<h5 class=\"card-title\">" + p.getTitle() + "</h5>"));
                            assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">" +
                                    p.getPrice().toString() + " руб.</span>"));
                            assertTrue(html.contains("<p class=\"card-text\">" + p.getDescription()+ "</p>"));
                           if (p.getCartItem() != null) {
                               assertTrue(html.contains("<span>" + p.getCartItem().getCount() + "</span>"));
                           }
                        });


        verify(productService).findProductByIdWithRelations(id);
        verifyNoMoreInteractions(productService);
        verifyNoInteractions(cartItemService);
    }

    @MethodSource("arguments_test_changeProductCountInTheCartOnProductPage")
    @ParameterizedTest
    public void test_changeProductCountInTheCartOnProductPage(Long id, String actionType) {
        Product p = productByIdTemplate(id).withCartItemByCount(1).get();
        when(cartItemService.changeProductCountInTheCart(id, ProductActionType.valueOf(actionType))).thenReturn(Mono.empty());
        when(productService.findProductByIdWithRelations(id)).thenReturn(Mono.just(p));

        webTestClient.post()
                .uri("/items/{id}", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", actionType))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<h5 class=\"card-title\">" + p.getTitle() + "</h5>"));
                    assertTrue(html.contains("<span class=\"badge text-bg-success justify-content-end\">" +
                            p.getPrice().toString() + " руб.</span>"));
                    assertTrue(html.contains("<p class=\"card-text\">" + p.getDescription()+ "</p>"));
                    if (p.getCartItem() != null) {
                        assertTrue(html.contains("<span>" + p.getCartItem().getCount() + "</span>"));
                    }
                });

        verify(cartItemService).changeProductCountInTheCart(id, ProductActionType.valueOf(actionType));
        verify(productService).findProductByIdWithRelations(id);
        verifyNoMoreInteractions(productService, cartItemService);

    }

    protected static Stream<Arguments> arguments_test_changeProductCountInTheCartOnProductPage() {
        return Stream.of(
                Arguments.of(1L, "PLUS"),
                Arguments.of(2L, "MINUS"),
                Arguments.of(3L, "DELETE")
        );
    }
}