package com.maono.marketapplication.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DeleteAction implements Strategy {
    private final CartItemRepository cartItemRepository;
    private final RedisCartItemRepository redisCartItemRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.DELETE;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return cartItemRepository.findById(id)
                .flatMap(this::dropCacheCount)
                .flatMap(cartItemRepository::delete);
    }

    private Mono<CartItem> dropCacheCount(CartItem cartItem) {
        cartItem.setCount(0);
        return redisCartItemRepository.cacheObject(cartItem);
    }
}
