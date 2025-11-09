package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresqlContainerConfiguration.class)
public class OrderServiceImplTest {
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected CartItemService cartItemService;

    @Test
    public void test_findAll() {
        List<Order> expected = List.of(
                buildOrder(1L, Pair.of(1L, 2), Pair.of(4L, 1)),
                buildOrder(2L, Pair.of(1L, 3), Pair.of(3L, 1), Pair.of(4L, 1), Pair.of(5L, 2))
        );
        assertEquals(expected, orderService.findAll());
    }

    @Test
    public void test_findById() {
        assertEquals(buildOrder(1L, Pair.of(1L, 2), Pair.of(4L, 1)), orderService.findById(1L));
    }

    @Test
    @Transactional
    public void test_buy() {
        assertFalse(cartItemService.findAll().isEmpty());
        assertThrows(NoSuchElementException.class, () -> orderService.findById(3L));
        orderService.buy();
        assertTrue(cartItemService.findAll().isEmpty());
        assertDoesNotThrow(() -> orderService.findById(3L));
        assertEquals(buildOrder(3L, Pair.of(1L, 3), Pair.of(2L, 1), Pair.of(5L, 2)), orderService.findById(3L));
    }
}
