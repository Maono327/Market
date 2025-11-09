package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.OrderController;
import com.maono.marketapplication.services.OrderService;
import com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrderById;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrderList;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.mapToOrderDto;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = {OrderService.class, OrderController.class})
@AutoConfigureMockMvc
class OrderControllerTest {
    @MockitoBean
    protected OrderService orderService;
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test_findAll() throws Exception {
        when(orderService.findAll()).thenReturn(buildOrderList(1L, 2L));

        mockMvc.perform(get("/orders"))
                .andExpect(model().attribute("orders", buildOrderList(1L, 2L).stream()
                        .map(ExpectedOrderAndOrderItemsTestDataProvider::mapToOrderDto).toList()))
                .andExpect(view().name("product_orders"))
                .andExpect(status().isOk());


        verify(orderService).findAll();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    protected void test_getOrder_default() throws Exception {
        when(orderService.findById(1L)).thenReturn(buildOrderById(1L));

        mockMvc.perform(get("/orders/{id}", 1))
                .andExpect(model().attribute("order", mapToOrderDto(buildOrderById(1L))))
                .andExpect(model().attribute("newOrder", false))
                .andExpect(view().name("product_order"))
                .andExpect(status().isOk());

        verify(orderService).findById(1L);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    protected void test_getOrder_newOrder() throws Exception {
        when(orderService.findById(1L)).thenReturn(buildOrderById(1L));

        mockMvc.perform(get("/orders/{id}", 1)
                        .param("newOrder", "true"))
                .andExpect(model().attribute("order", mapToOrderDto(buildOrderById(1L))))
                .andExpect(model().attribute("newOrder", true))
                .andExpect(view().name("product_order"))
                .andExpect(status().isOk());

        verify(orderService).findById(1L);
        verifyNoMoreInteractions(orderService);
    }

}