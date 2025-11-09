package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.services.implementations.CartItemServiceImpl;
import com.maono.marketapplication.util.ExpectedCartItemTestDataProvider;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemByProductId;
import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemsList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductById;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CartItemServiceImpl.class, CartItemRepository.class})
class CartItemServiceImplTest {
    @Autowired
    protected CartItemServiceImpl cartItemService;
    @MockitoBean
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected ProductService productService;

    @Test
    public void test_findAll() {
        when(cartItemRepository.findAll())
                .thenReturn(buildCartItemsList(Pair.of(1L, 2), Pair.of(3L, 1), Pair.of(5L, 1)));
        List<CartItem> expected = buildCartItemsList(Pair.of(1L, 2), Pair.of(3L, 1), Pair.of(5L, 1));
        assertEquals(expected, cartItemService.findAll());
        verify(cartItemRepository).findAll();
        verifyNoMoreInteractions(cartItemRepository);
        verifyNoInteractions(productService);
    }

    @Test
    public void test_removeAll() {
        doNothing().when(cartItemRepository).deleteAll();
        when(cartItemRepository.findAll()).thenReturn(List.of(
                buildCartItemByProductId(1L, 1),
                buildCartItemByProductId(2L, 2)));
        cartItemService.removeAll();
        verify(cartItemRepository).deleteAll();
        verify(cartItemRepository).findAll();
        verifyNoMoreInteractions(cartItemRepository);
        verifyNoInteractions(productService);
    }

    @MethodSource("arguments_test_changeProductCountInTheCart")
    @ParameterizedTest
    public void test_changeProductCountInTheCart(ProductActionType actionType,
                                                 Long productId,
                                                 CartItem foundling,
                                                 int expectedCount) {
        if (foundling == null && actionType == ProductActionType.PLUS) {
            test_changeProductInTheCart_newCartItem(actionType, productId, expectedCount);
        } else if (foundling == null &&
                (actionType == ProductActionType.MINUS || actionType == ProductActionType.DELETE)) {
            test_changeProductInTheCart_nothingChange(productId, actionType);
        } else {
            test_changeProductInTheCart_existsItem(actionType, productId, foundling, expectedCount);
        }
    }

    protected void test_changeProductInTheCart_newCartItem(ProductActionType actionType,
                                                           Long productId,
                                                           int expectedCount) {
        when(cartItemRepository.findById(productId)).thenReturn(Optional.empty());
        when(productService.findProductById(productId)).thenReturn(buildProductById(productId));

        cartItemService.changeProductCountInTheCart(productId, actionType);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        assertEquals(expectedCount, cartItemArgumentCaptor.getValue().getCount());

        verify(cartItemRepository).findById(productId);
        verify(cartItemRepository).save(any(CartItem.class));
        verify(productService).findProductById(productId);
        verifyNoMoreInteractions(cartItemRepository, productService);
    }

    protected void test_changeProductInTheCart_nothingChange(Long productId, ProductActionType actionType) {
        cartItemService.changeProductCountInTheCart(productId, actionType);
        verify(cartItemRepository).findById(productId);
        verifyNoMoreInteractions(cartItemRepository);
        verifyNoInteractions(productService);
    }

    protected void test_changeProductInTheCart_existsItem(ProductActionType actionType,
                                                           Long productId,
                                                           CartItem foundling,
                                                           int expectedCount) {
        switch (actionType) {
            case PLUS -> {
                when(cartItemRepository.findById(productId)).thenReturn(Optional.ofNullable(foundling));

                cartItemService.changeProductCountInTheCart(productId, actionType);

                ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
                verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
                assertEquals(expectedCount, cartItemArgumentCaptor.getValue().getCount());

                verify(cartItemRepository).findById(productId);
                verifyNoMoreInteractions(cartItemRepository);
                verifyNoInteractions(productService);
            }
            case MINUS -> {
                when(cartItemRepository.findById(productId)).thenReturn(Optional.ofNullable(foundling));

                if (foundling.getCount() == 1) {
                    doNothing().when(cartItemRepository).delete(CartItem.builder().id(foundling.getId()).count(foundling.getCount()).build());

                    cartItemService.changeProductCountInTheCart(productId, actionType);

                    ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
                    verify(cartItemRepository).delete(cartItemArgumentCaptor.capture());
                    assertEquals(expectedCount, cartItemArgumentCaptor.getValue().getCount());

                    verify(cartItemRepository).findById(productId);
                    verify(cartItemRepository).delete(CartItem.builder().id(foundling.getId()).count(foundling.getCount()).build());
                } else {
                    cartItemService.changeProductCountInTheCart(productId, actionType);

                    ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
                    verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
                    assertEquals(expectedCount, cartItemArgumentCaptor.getValue().getCount());

                    verify(cartItemRepository).findById(productId);
                    verify(cartItemRepository).save(foundling);
                }
                verifyNoMoreInteractions(cartItemRepository);
                verifyNoInteractions(productService);
            }
            case DELETE -> {
                when(cartItemRepository.findById(productId)).thenReturn(Optional.ofNullable(foundling));

                doNothing().when(cartItemRepository).delete(CartItem.builder().id(foundling.getId()).count(foundling.getCount()).build());

                cartItemService.changeProductCountInTheCart(productId, actionType);

                ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
                verify(cartItemRepository).delete(cartItemArgumentCaptor.capture());
                assertEquals(expectedCount, cartItemArgumentCaptor.getValue().getCount());

                verify(cartItemRepository).findById(productId);
                verify(cartItemRepository).delete(CartItem.builder().id(foundling.getId()).count(foundling.getCount()).build());
                verifyNoMoreInteractions(cartItemRepository);
                verifyNoInteractions(productService);
            }
        }
    }


    private static Stream<Arguments> arguments_test_changeProductCountInTheCart() {
        return Stream.of(
            Arguments.of(ProductActionType.PLUS, 1L, null, 1),
            Arguments.of(ProductActionType.PLUS, 1L, buildCartItemByProductId(1L, 2), 3),
            Arguments.of(ProductActionType.MINUS, 1L, buildCartItemByProductId(1L, 1), 0),
            Arguments.of(ProductActionType.MINUS, 2L, buildCartItemByProductId(2L, 2), 1),
            Arguments.of(ProductActionType.MINUS, 2L, null, -1),
            Arguments.of(ProductActionType.DELETE, 3L, buildCartItemByProductId(3L, 2), 2),
            Arguments.of(ProductActionType.DELETE, 3L, null, -1)
        );
    }
}