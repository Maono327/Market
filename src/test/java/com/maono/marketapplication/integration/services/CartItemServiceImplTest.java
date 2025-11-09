package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemByProductId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresqlContainerConfiguration.class)
public class CartItemServiceImplTest {
    @Autowired
    protected CartItemService cartItemService;
    @Autowired
    protected ProductService productService;
    @Autowired
    protected CartItemRepository cartItemRepository;

    @Test
    @Transactional
    public void test_changeProductCountInTheCart_PLUS() {
        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());
        cartItemService.changeProductCountInTheCart(1L, ProductActionType.PLUS);
        assertEquals(4, productService.findProductById(1L).getCartItem().getCount());
    }


    @Test
    @Transactional
    public void test_changeProductCountInTheCart_MINUS() {
        assertEquals(3, productService.findProductById(1L).getCartItem().getCount());
        cartItemService.changeProductCountInTheCart(1L, ProductActionType.MINUS);
        assertEquals(2, productService.findProductById(1L).getCartItem().getCount());
    }


    @Test
    @Transactional
    public void test_changeProductCountInTheCart_DELETE() {
        assertEquals(Optional.ofNullable(buildCartItemByProductId(1L, 3)), cartItemRepository.findById(1L));
        cartItemService.changeProductCountInTheCart(1L, ProductActionType.DELETE);
        assertEquals(Optional.empty(), cartItemRepository.findById(1L));
    }

    @Test
    public void test_findAll() {
        List<CartItem> expected = List.of(
                    buildCartItemByProductId(1L, 3),
                    buildCartItemByProductId(2L, 1),
                    buildCartItemByProductId(5L, 2)

                );

        assertEquals(expected, cartItemService.findAll());
    }

    @Test
    @Transactional
    public void test_removeAll() {
        assertFalse(cartItemService.findAll().isEmpty());
        cartItemService.removeAll();
        assertTrue(cartItemService.findAll().isEmpty());
    }
}
