package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.repositories.OrderItemRepository;
import com.maono.marketapplication.repositories.OrderRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.implementations.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItemList;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderServiceImpl.class})
class OrderServiceImplTest {
    @Autowired
    protected OrderServiceImpl orderService;
    @MockitoBean
    protected OrderRepository orderRepository;
    @MockitoBean
    protected CartItemService cartItemService;
    @MockitoBean
    protected OrderItemRepository orderItemRepository;

    @Test
    public void test_findAllWithRelations() {
        List<Order> orders = List.of(
                    order(1L, BigDecimal.valueOf(101 * 2 + 103))
                            .withOrderItems()
                            .orderItem(1L, 2)
                            .orderItem(3L, 1)
                            .getItems()
                            .get(),
                    order(2L, BigDecimal.valueOf(102 * 3 + 104 * 3))
                            .withOrderItems()
                            .orderItem(2L, 3)
                            .orderItem(4L, 3)
                            .getItems()
                            .get()
                );

        when(orderRepository.findAllWithRelations()).thenReturn(Flux.fromIterable(orders));

        StepVerifier.create(orderService.findAllWithRelations())
                .expectNextCount(2)
                .verifyComplete();

        verify(orderRepository).findAllWithRelations();
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(cartItemService, orderItemRepository);
    }

    @Test
    public void test_findByIdWithRelations() {
        when(orderRepository.findByIdWithRelations(1L)).thenReturn(
                Mono.just(
                    order(1L, BigDecimal.valueOf(102*3 + 103*2))
                        .withOrderItems()
                        .orderItem(2L, 3)
                        .orderItem(3L, 2)
                        .getItems()
                        .get())
        );

        StepVerifier.create(orderService.findByIdWithRelations(1L))
                .expectNextCount(1)
                .verifyComplete();

        verify(orderRepository).findByIdWithRelations(1L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(cartItemService, orderItemRepository);
    }

    @Test
    public void test_buy() {
        BigDecimal totalSum = BigDecimal.valueOf(101 * 2 + 102 * 3 + 103 + 104 * 4);
        when(cartItemService.findAllWithRelations()).thenReturn(Flux.fromIterable(cartItemList(List.of(2, 3, 1, 4))));
        when(cartItemService.calculateTotalSum(anyList())).thenReturn(totalSum);
        when(orderItemRepository.saveOrderItems(anyList())).thenReturn(Mono.empty());
        when(cartItemService.removeAll()).thenReturn(Mono.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return Mono.just(order);
        });

        Order expected = order(1L, BigDecimal.valueOf(101 * 2 + 102 * 3 + 103 + 104 * 4)).get();

        StepVerifier.create(orderService.buy())
                .assertNext(order -> {
                    assertEquals(expected, order);
                })
                .verifyComplete();
    }
}