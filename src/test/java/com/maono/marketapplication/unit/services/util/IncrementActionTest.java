package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.util.IncrementAction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = IncrementAction.class)
public class IncrementActionTest {
    @Autowired
    protected IncrementAction incrementAction;
    @MockitoBean
    ProductRepository productRepository;
    @MockitoBean
    CartItemRepository cartItemRepository;

    @Test
    public void test_executeChange_incrementCount() {
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem(1L, 2).get()));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(incrementAction.executeChange(1L))
                .verifyComplete();

        ArgumentCaptor<CartItem> incrementSaveCaptor = ArgumentCaptor.forClass(CartItem.class);

        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(incrementSaveCaptor.capture());
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(cartItemRepository, productRepository);

        CartItem saved = incrementSaveCaptor.getValue();
        assertEquals(3, saved.getCount());
        assertFalse(saved.isNew());
    }

    @Test
    public void test_executeChange_newCartItem() {
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.empty());
        when(productRepository.findById(1L)).thenReturn(Mono.just(productByIdTemplate(1L).get()));

        StepVerifier.create(incrementAction.executeChange(1L))
                .verifyComplete();

        ArgumentCaptor<CartItem> saveCaptor = ArgumentCaptor.forClass(CartItem.class);

        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(saveCaptor.capture());
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(cartItemRepository, productRepository);

        CartItem saved = saveCaptor.getValue();
        assertEquals(1, saved.getCount());
        assertTrue(saved.isNew());
    }

}
