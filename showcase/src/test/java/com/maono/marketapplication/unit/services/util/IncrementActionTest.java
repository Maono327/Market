package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.services.util.IncrementAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = IncrementAction.class)
public class IncrementActionTest {
    @Autowired
    protected IncrementAction incrementAction;
    @MockitoBean
    RedisCartItemRepository redisCartItemRepository;
    @MockitoBean
    CartItemRepository cartItemRepository;

    @Test
    public void test_executeChange_incrementCount_CartItemCached() {
        CartItem cachedCartItem = cartItem(1L, 2).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.just(cachedCartItem));

        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());

        CartItem savable = cartItem(1L, 3).get();
        when(cartItemRepository.save(eq(savable))).thenReturn(Mono.just(savable));
        when(redisCartItemRepository.cacheObject(eq(savable))).thenReturn(Mono.just(savable));

        StepVerifier.create(incrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(eq(cartItem(1L, 3).get()));
        verify(redisCartItemRepository).cacheObject(eq(cartItem(1L, 3).get()));

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }

    @Test
    public void test_executeChange_incrementCount_CartItemNotCached() {
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());

        CartItem cartItem = cartItem(1L, 2).get();
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem));
        CartItem incremented = cartItem(1L, 3).get();
        when(cartItemRepository.save(eq(incremented))).thenReturn(Mono.just(incremented));
        when(redisCartItemRepository.cacheObject(eq(incremented))).thenReturn(Mono.just(incremented));

        StepVerifier.create(incrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(eq(cartItem(1L, 3).get()));
        verify(redisCartItemRepository).cacheObject(eq(cartItem(1L, 3).get()));

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }

    @Test
    public void test_executeChange_incrementCount_CartItemNotExist() {
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());

        when(cartItemRepository.save(argThat(cartItem ->
                cartItem.getId() == 1L && cartItem.getCount() == 1 && cartItem.isNew())))
                .thenReturn(Mono.just(cartItem(1L, 1).get()));
        when(redisCartItemRepository.cacheObject(eq(cartItem(1L, 1).get())))
                .thenReturn(Mono.just(cartItem(1L, 1).get()));

        StepVerifier.create(incrementAction.executeChange(1L))
                .verifyComplete();

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).save(argThat(cartItem ->
                cartItem.getId() == 1L && cartItem.getCount() == 1 && cartItem.isNew()));
        verify(redisCartItemRepository).cacheObject(cartItem(1l,1).get());

        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
    }

}
