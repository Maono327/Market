package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductByIdWithCartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.mapToProductDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlContainerConfiguration.class)
public class CartControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test_getCartItems() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(
                        mapToProductDto(buildProductByIdWithCartItem(1L, 3)),
                        mapToProductDto(buildProductByIdWithCartItem(2L, 1)),
                        mapToProductDto(buildProductByIdWithCartItem(5L, 2))
                )))
                .andExpect(model().attribute("total", BigDecimal.valueOf(32099.95)))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_PLUS() throws Exception {
        mockMvc.perform(post("/cart/items")
                .param("id", "1")
                .param("action", "PLUS"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(
                        mapToProductDto(buildProductByIdWithCartItem(2L, 1)),
                        mapToProductDto(buildProductByIdWithCartItem(5L, 2)),
                        mapToProductDto(buildProductByIdWithCartItem(1L, 4))
                )))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_MINUS() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .param("id", "2")
                        .param("action", "MINUS"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(
                        mapToProductDto(buildProductByIdWithCartItem(1L, 3)),
                        mapToProductDto(buildProductByIdWithCartItem(5L, 2))
                )))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_DELETE() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .param("id", "5")
                        .param("action", "DELETE"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(
                        mapToProductDto(buildProductByIdWithCartItem(1L, 3)),
                        mapToProductDto(buildProductByIdWithCartItem(2L, 1))
                )))
                .andExpect(status().isOk());
    }
}
