package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.implementations.CartItemServiceImpl;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ExpectedCartItemTestDataProvider;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CartItemServiceImpl.class})
class CartItemServiceImplTest {
    @Autowired
    protected CartItemServiceImpl cartItemService;
    @MockitoBean
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected ProductRepository productRepository;
    @MockitoBean
    protected ProductActionStrategy productActionStrategy;

    @Test
    public void test_changeProductCountInTheCart() {
        when(productActionStrategy.execute(any(ProductActionType.class), anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.PLUS))
                        .verifyComplete();
        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.MINUS))
                        .verifyComplete();
        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.DELETE))
                        .verifyComplete();

        verify(productActionStrategy, times(3)).execute(any(), anyLong());
        verifyNoMoreInteractions(productActionStrategy);
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    @Test
    public void test_findAllWithRelations() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                cartItem(1L, 3).get(),
                cartItem(2L, 1).get(),
                cartItem(4L, 2).get()));
        when(productRepository.findAllById(anyList())).thenReturn(Flux.just(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(4L).get()
        ));

        Flux<CartItem> cartItemFlux = cartItemService.findAllWithRelations();

        StepVerifier.create(cartItemFlux)
                .assertNext(cartItem ->
                        assertEquals(cartItem(1L, 3).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(2L, 1).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(4L, 2).withProductByTemplate().get(), cartItem))
                .verifyComplete();

        verify(cartItemRepository).findAll();
        verify(productRepository).findAllById(List.of(1L, 2L, 4L));
        verifyNoMoreInteractions(cartItemRepository, productRepository);
        verifyNoInteractions(productActionStrategy);
    }

    @Test
    public void test_removeAll() {
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.removeAll()).verifyComplete();

        verify(cartItemRepository).deleteAll();
        verifyNoMoreInteractions(cartItemRepository);
        verifyNoInteractions(productRepository, productActionStrategy);
    }

    @Test
    public void test_calculateTotalSum() {
        List<CartItem> cartItemList = ExpectedCartItemTestDataProvider.cartItemList(List.of(1, 2, 3, 4));
        BigDecimal expected = BigDecimal.valueOf(101 + 102*2 + 103*3 + 104*4);

        assertEquals(expected, cartItemService.calculateTotalSum(cartItemList));
        verifyNoInteractions(cartItemRepository, cartItemRepository, productActionStrategy);
    }
}