package com.maono.marketapplication.integration.repositories;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.OrderRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrder;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrderById;
import static com.maono.marketapplication.util.ExpectedOrderAndOrderItemsTestDataProvider.buildOrderWithManagedEntities;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(PostgresqlContainerConfiguration.class)
public class OrderRepositoryTest {

    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected ProductRepository productRepository;

    @Test
    public void test_findAll() {
        List<Order> expected = List.of(buildOrderById(1L), buildOrderById(2L));

        assertEquals(expected, orderRepository.findAll());
    }

    @Test
    @Transactional
    public void test_save() {
        Order order = buildOrderWithManagedEntities(
                Pair.of(findProductById(1L), 4),
                Pair.of(findProductById(2L), 2),
                Pair.of(findProductById(5L), 3));

        Order expected = buildOrder(3L,
                Pair.of(1L, 4),
                Pair.of(2L, 2),
                Pair.of(5L, 3));

        assertEquals(Optional.empty(), orderRepository.findById(3L));
        Order saved = orderRepository.save(order);
        assertEquals(3L, saved.getId());
        assertEquals(expected, saved);
    }

    protected Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L, 6L})
    @ParameterizedTest
    public void test_findById(Long id) {
        Optional<Order> expected = Optional.ofNullable(buildOrderById(id));
        assertEquals(expected, orderRepository.findById(id));
    }
}
