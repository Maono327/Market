package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.dto.ProductsPageParametersDto;
import com.maono.marketapplication.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductByIdWithCartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductDtoList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.mapToProductDto;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.stubProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlContainerConfiguration.class)
public class ProductControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ProductService productService;

    @Test
    public void test_getProducts_emptySearch() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items",
                        List.of(buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(1L, 3),
                                        buildProductByIdWithCartItem(2L, 1),
                                        buildProductByIdWithCartItem(3L, 0))),
                                buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(4L, 0),
                                        buildProductByIdWithCartItem(5L, 2),
                                        stubProduct())))))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(5, 1, false, false)))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getProducts_set_search() throws Exception {
        mockMvc.perform(get("/items")
                        .param("search", "Кн"))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items",
                        List.of(buildProductDtoList(List.of(
                                    buildProductByIdWithCartItem(1L, 3),
                                    stubProduct(),
                                    stubProduct())))))
                .andExpect(model().attribute("search", "Кн"))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(5, 1, false, false)))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getProducts_set_sort_ALPHA() throws Exception {
        mockMvc.perform(get("/items").param("sort", "ALPHA"))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items",
                        List.of(buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(3L, 0),
                                        buildProductByIdWithCartItem(5L, 2),
                                        buildProductByIdWithCartItem(4L, 0))),
                                buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(1L, 3),
                                        buildProductByIdWithCartItem(2L, 1),
                                        stubProduct())))))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(5, 1, false, false)))
                .andExpect(model().attribute("sort", "ALPHA"))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getProducts_set_sort_PRICE() throws Exception {
        mockMvc.perform(get("/items").param("sort", "PRICE"))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items",
                        List.of(buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(1L, 3),
                                        buildProductByIdWithCartItem(4L, 0),
                                        buildProductByIdWithCartItem(2L, 1))),
                                buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(3L, 0),
                                        buildProductByIdWithCartItem(5L, 2),
                                        stubProduct())))))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(5, 1, false, false)))
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getProducts_set_pageSize_and_pageNumber() throws Exception {
        mockMvc.perform(get("/items")
                            .param("pageSize", "2")
                            .param("pageNumber", "2"))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items",
                        List.of(buildProductDtoList(List.of(
                                        buildProductByIdWithCartItem(3L, 0),
                                        buildProductByIdWithCartItem(4L, 0),
                                        stubProduct()
                                )))))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(2, 2, true, true)))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_PLUS() throws Exception {

        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items?search=&sort=NO&pageSize=5&pageNumber=1"));

        assertEquals(4, productService.findProductById(1L).getCartItem().getCount());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_MINUS() throws Exception {

        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items?search=&sort=NO&pageSize=5&pageNumber=1"));

        assertEquals(2, productService.findProductById(1L).getCartItem().getCount());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_PLUS_with_params() throws Exception {

        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());

        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "MINUS")
                        .param("search", "кн")
                        .param("pageSize", "2",
                                     "pageNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items?search=%D0%BA%D0%BD&sort=NO&pageSize=2&pageNumber=1"));

        assertEquals(2, productService.findProductById(1L).getCartItem().getCount());
    }

    @MethodSource("arguments_test_getProduct")
    @ParameterizedTest
    public void test_getProduct(Pair<Long, Integer> product) throws Exception {
        mockMvc.perform(get("/items/{id}", product.getFirst()))
                .andExpect(view().name("product_item"))
                .andExpect(model().attribute("item",
                        mapToProductDto(buildProductByIdWithCartItem(product.getFirst(), product.getSecond()))))
                .andExpect(status().isOk());
    }

    protected static Stream<Pair<Long, Integer>> arguments_test_getProduct() {
        return Stream.of(
                Pair.of(1L, 3),
                Pair.of(2L, 1),
                Pair.of(3L, 0),
                Pair.of(4L, 0),
                Pair.of(5L, 2)
        );
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart2_PLUS() throws Exception {
        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());
        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "PLUS"))
                .andExpect(view().name("product_item"))
                .andExpect(model().attribute("item",
                        mapToProductDto(buildProductByIdWithCartItem(1L, 4))))
                .andExpect(status().isOk());
        assertEquals(4, productService.findProductById(1L).getCartItem().getCount());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart2_MINUS() throws Exception {
        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());
        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "MINUS"))
                .andExpect(view().name("product_item"))
                .andExpect(model().attribute("item",
                        mapToProductDto(buildProductByIdWithCartItem(1L, 2))))
                .andExpect(status().isOk());
        assertEquals(2, productService.findProductById(1L).getCartItem().getCount());
    }
}

