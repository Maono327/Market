package com.maono.marketapplication.integration.controllers;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresqlContainerConfiguration.class)
public class OperationsControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected OrderService orderService;

    @Test
    @Transactional
    public void test_createOrder() throws Exception {
        assertThrows(NoSuchElementException.class, () -> orderService.findById(3L));
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/3?newOrder=true"));
        assertEquals(buildOrder(3L, Pair.of(1L, 3), Pair.of(2L, 1), Pair.of(5L, 2)), orderService.findById(3L));
    }
}
