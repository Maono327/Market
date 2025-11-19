package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.services.util.DecrementAction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = DecrementAction.class)
public class DecrementActionTest {
    @Autowired
    protected DecrementAction decrementAction;
    @MockitoBean(reset = MockReset.AFTER)
    protected CartItemRepository cartItemRepository;

    @Test
    public void test_executeChange_decrement() {
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem(1L, 3).get()));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        StepVerifier.create(decrementAction.executeChange(1L))
                .verifyComplete();

        ArgumentCaptor<CartItem> decrementCartItem = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(decrementCartItem.capture());
        verify(cartItemRepository, never()).delete(any());

        CartItem saved = decrementCartItem.getValue();
        assertEquals(2, saved.getCount());
        assertFalse(saved.isNew());
    }


    @Test
    public void test_executeChange_delete() {
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem(1L, 1).get()));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());

        StepVerifier.create(decrementAction.executeChange(1L))
                .verifyComplete();

        ArgumentCaptor<CartItem> decrementCartItem = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository).delete(decrementCartItem.capture());

        CartItem saved = decrementCartItem.getValue();
        assertEquals(0, saved.getCount());
    }
}
