package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.CartController;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemsList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductDtoList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = {CartController.class, CartItemService.class})
@AutoConfigureMockMvc
class CartControllerTest {
    @MockitoBean
    protected CartItemService cartItemService;
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test_getCartItems() throws Exception {
        when(cartItemService.findAll()).thenReturn(getCartItemList());
        when(cartItemService.calculateTotalSum(anyList())).thenReturn(BigDecimal.valueOf(15399.97));
        mockMvc.perform(get("/cart/items"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items",
                        buildProductDtoList(unwrapProductsFromCartItems(getCartItemList()))))
                .andExpect(model().attribute("total", BigDecimal.valueOf(15399.97)))
                .andExpect(status().isOk());

        verify(cartItemService).calculateTotalSum(anyList());
        verify(cartItemService).findAll();
        verifyNoMoreInteractions(cartItemService);
    }

    @Test
    public void test_changeProductCountInTheCart() throws Exception {
        doNothing().when(cartItemService).changeProductCountInTheCart(1L, ProductActionType.PLUS);
        when(cartItemService.findAll()).thenReturn(buildCartItemsList(Pair.of(1L, 3), Pair.of(2L, 3), Pair.of(4L, 1)));
        when(cartItemService.calculateTotalSum(anyList())).thenReturn(BigDecimal.valueOf(16099.96));
        mockMvc.perform(post("/cart/items")
                    .param("id", "1")
                    .param("action", "PLUS"))
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items",
                        buildProductDtoList(unwrapProductsFromCartItems(
                                buildCartItemsList(Pair.of(1L, 3), Pair.of(2L, 3), Pair.of(4L, 1))))))
                .andExpect(model().attribute("total", BigDecimal.valueOf(16099.96)))
                .andExpect(status().isOk());

        verify(cartItemService).changeProductCountInTheCart(1L, ProductActionType.PLUS);
        verify(cartItemService).calculateTotalSum(anyList());
        verify(cartItemService).findAll();
        verifyNoMoreInteractions(cartItemService);
    }

    protected static List<CartItem> getCartItemList() {
        return buildCartItemsList(Pair.of(1L, 2), Pair.of(2L, 3), Pair.of(4L, 1));
    }

    protected static List<Product> unwrapProductsFromCartItems(List<CartItem> cartItems) {
        return cartItems.stream().map(CartItem::getProduct).toList();
    }
}