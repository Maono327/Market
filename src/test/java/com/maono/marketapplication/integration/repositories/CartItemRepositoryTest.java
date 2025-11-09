package com.maono.marketapplication.integration.repositories;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemByProductId;
import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemWithManagedEntity;
import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemsList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DataJpaTest
@Import(PostgresqlContainerConfiguration.class)
class CartItemRepositoryTest {
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected ProductRepository productRepository;

    @MethodSource("arguments_test_findById")
    @ParameterizedTest
    public void test_findById(long id, int count) {
        Optional<CartItem> expected = count != -1 ?
                Optional.ofNullable(buildCartItemByProductId(id, count)) :
                Optional.empty();

        assertEquals(expected, cartItemRepository.findById(id));
    }

    public static Stream<Arguments> arguments_test_findById() {
        return Stream.of(
                Arguments.of(1L, 3),
                Arguments.of(2L, 1),
                Arguments.of(5L, 2),
                Arguments.of(6L, -1),
                Arguments.of(7L, -1),
                Arguments.of(8L, -1)
        );
    }

    @Test
    public void test_findAll() {
        List<CartItem> expected = buildCartItemsList(Pair.of(1L, 3), Pair.of(2L, 1), Pair.of(5L, 2));
        assertEquals(expected, cartItemRepository.findAll());
    }

    @Test
    @Transactional
    public void test_save() {
        CartItem cartItem = buildCartItemWithManagedEntity(findProductById(3L), 2);
        CartItem expected = buildCartItemByProductId(3L, 2);
        assertEquals(Optional.empty(), cartItemRepository.findById(3L));
        CartItem saved = cartItemRepository.save(cartItem);
        assertEquals(3L, saved.getId());
        assertEquals(expected, saved);
    }

    @Test
    @Transactional
    public void test_delete() {
        assertNotEquals(Optional.empty(), cartItemRepository.findById(1L));
        CartItem cartItem = cartItemRepository.findById(1L).get();
        cartItem.getProduct().setCartItem(null);
        cartItem.setProduct(null);
        cartItemRepository.delete(cartItem);
        assertEquals(Optional.empty(), cartItemRepository.findById(1L));
    }

    @Test
    @Transactional
    public void test_deleteAll() {
        assertNotEquals(Collections.emptyList(), cartItemRepository.findAll());
        cartItemRepository.findAll().stream().forEach(item -> {
            item.getProduct().setCartItem(null);
            item.setProduct(null);
        });
        cartItemRepository.deleteAll();
        assertEquals(Collections.emptyList(), cartItemRepository.findAll());
    }

    protected Product findProductById(long id) {
        return productRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}