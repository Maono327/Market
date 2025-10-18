package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrder;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.mapToOrderDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlContainerConfiguration.class)
public class OrderControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test_getOrders() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(view().name("product_orders"))
                .andExpect(model().attribute("orders", List.of(
                        mapToOrderDto(buildOrder(1L, Pair.of(1L, 2), Pair.of(4L, 1))),
                        mapToOrderDto(buildOrder(2L, Pair.of(1L, 3), Pair.of(3L, 1),
                                                             Pair.of(4L, 1), Pair.of(5L, 2)))
                )))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}", 1))
                .andExpect(view().name("product_order"))
                .andExpect(model().attribute("order", mapToOrderDto(buildOrder(1L,
                        Pair.of(1L, 2), Pair.of(4L, 1)))))
                .andExpect(model().attribute("newOrder", false))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getOrder_newOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}", 1)
                        .param("newOrder", "true"))
                .andExpect(view().name("product_order"))
                .andExpect(model().attribute("order", mapToOrderDto(buildOrder(1L,
                        Pair.of(1L, 2), Pair.of(4L, 1)))))
                .andExpect(model().attribute("newOrder", true))
                .andExpect(status().isOk());
    }
}
