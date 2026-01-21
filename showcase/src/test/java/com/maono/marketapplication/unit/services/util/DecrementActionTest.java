package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.services.util.DecrementAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DecrementAction.class)
public class DecrementActionTest {
    @Autowired
    protected DecrementAction decrementAction;
    @MockitoBean(reset = MockReset.AFTER)
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected RedisCartItemRepository redisCartItemRepository;

    @Test
    public void test_executeChange_decrement_cartItemCached() {
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.just(cartItem(1L, 3).get()));

        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());
        when(redisCartItemRepository.cacheObject(cartItem(1L, 0).get())).thenReturn(Mono.empty());

        CartItem decrementedCartItem = cartItem(1L, 2).get();
        when(cartItemRepository.save(decrementedCartItem)).thenReturn(Mono.just(decrementedCartItem));
        when(redisCartItemRepository.cacheObject(decrementedCartItem)).thenReturn(Mono.just(decrementedCartItem));

        StepVerifier.create(decrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository).cacheObject(cartItem(1L, 0).get());
        verify(cartItemRepository).save(cartItem(1L, 2).get());
        verify(redisCartItemRepository).cacheObject(cartItem(1L, 2).get());

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }


    @Test
    public void test_executeChange_decrement_cartItemNotCached() {
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());

        CartItem cartItemFromDb = cartItem(1L, 3).get();
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItemFromDb));
        when(redisCartItemRepository.cacheObject(cartItem(1L, 0).get())).thenReturn(Mono.empty());

        CartItem decrementedCartItem = cartItem(1L, 2).get();
        when(cartItemRepository.save(decrementedCartItem)).thenReturn(Mono.just(decrementedCartItem));
        when(redisCartItemRepository.cacheObject(decrementedCartItem)).thenReturn(Mono.just(decrementedCartItem));

        StepVerifier.create(decrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository).cacheObject(cartItem(1L, 0).get());
        verify(cartItemRepository).save(decrementedCartItem);
        verify(redisCartItemRepository).cacheObject(decrementedCartItem);

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }

    @Test
    public void test_executeChange_decrement_cartItemNotExist() {
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());

        CartItem cartItem = cartItem(1L, 0).get();
        when(redisCartItemRepository.cacheObject(cartItem)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(decrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository).cacheObject(cartItem(1L, 0).get());
        verify(cartItemRepository, never()).save(any(CartItem.class));

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }

    @Test
    public void test_executeChange_delete_cartItemCached() {
        CartItem cached = cartItem(1L, 1).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.just(cached));
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());
        when(redisCartItemRepository.cacheObject(cartItem(1L, 0).get())).thenReturn(Mono.empty());
        when(cartItemRepository.delete(cached)).thenReturn(Mono.empty());

        StepVerifier.create(decrementAction.executeChange(1L))
                        .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository, times(2)).cacheObject(cartItem(1L, 0).get());
        verify(cartItemRepository).delete(cached);

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }
}
