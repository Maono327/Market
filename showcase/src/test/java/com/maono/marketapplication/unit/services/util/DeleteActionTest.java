package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.services.util.DeleteAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = DeleteAction.class)
public class DeleteActionTest {
    @Autowired
    protected DeleteAction deleteAction;
    @MockitoBean
    CartItemRepository cartItemRepository;

    @Test
    public void test_executeChange() {
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem(1L, 2).get()));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());

        StepVerifier.create(deleteAction.executeChange(1L))
                .verifyComplete();

        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).delete(cartItem(1L, 2).get());
        verifyNoMoreInteractions(cartItemRepository);
    }
}
