package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.OperationsController;
import com.maono.marketapplication.services.OrderService;
import com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {OrderService.class, OperationsController.class})
@AutoConfigureMockMvc
class OperationsControllerTest {
    @MockitoBean
    protected OrderService orderService;
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test_createOrder() throws Exception {
        Mockito.when(orderService.buy())
                .thenReturn(ExpectedOrderAndOrderItemsTestDataProvider.buildOrderById(1L));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1?newOrder=true"));

        verify(orderService).buy();
        verifyNoMoreInteractions(orderService);
    }
}