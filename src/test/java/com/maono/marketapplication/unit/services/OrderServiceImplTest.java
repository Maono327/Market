package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.repositories.OrderRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.implementations.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemsList;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrder;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrderById;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderServiceImpl.class, OrderRepository.class})
class OrderServiceImplTest {
    @Autowired
    protected OrderServiceImpl orderService;
    @MockitoBean
    protected OrderRepository orderRepository;
    @MockitoBean
    protected CartItemService cartItemService;

    @Test
    public void test_findAll() {
        when(orderRepository.findAll()).thenReturn(List.of(buildOrderById(1L), buildOrderById(2L)));
        List<Order> expected = List.of(buildOrderById(1L), buildOrderById(2L));
        List<Order> result = orderService.findAll();
        assertEquals(expected, result);
        verify(orderRepository).findAll();
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(cartItemService);
    }

    @Test
    public void test_findById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(buildOrderById(1L)));
        Optional<Order> expected = Optional.ofNullable(buildOrderById(1L));
        assertEquals(expected, orderRepository.findById(1L));
        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(cartItemService);
    }

    @Test
    public void test_findById_throw_NoSuchElementException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> orderService.findById(1L));
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).findAll();
        verify(orderRepository, never()).save(any());
        verify(cartItemService, never()).findAll();
        verify(cartItemService, never()).removeAll();
    }

    @Test
    public void test_buy() {
        when(cartItemService.findAll()).thenReturn(buildCartItemsList(Pair.of(1L, 2), Pair.of(2L, 1), Pair.of(5L, 2)));
        Order saveable = buildOrder(Pair.of(1L, 2), Pair.of(2L, 1), Pair.of(5L, 2));
        when(orderRepository.save(saveable)).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(3L);
            o.getItems().forEach(item -> {
                item.getId().setOrderId(3L);
                item.getId().setProductId(item.getProduct().getId());
            });
            return o;
        });
        Order expected = buildOrder(3L, Pair.of(1L, 2), Pair.of(2L, 1), Pair.of(5L, 2));
        assertEquals(expected, orderService.buy());
        verify(cartItemService).findAll();
        verify(cartItemService).removeAll();
        verify(orderRepository).save(any(Order.class));
        verify(orderRepository, never()).findById(anyLong());
        verifyNoMoreInteractions(orderRepository, cartItemService);
    }
}