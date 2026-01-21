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
public class IncrementAction implements Strategy {
    private final CartItemRepository cartItemRepository;
    private final RedisCartItemRepository redisCartItemRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.PLUS;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return updateCachedCount(id)
                .switchIfEmpty(
                        updateCartItemCountAndCache(id)
                                .switchIfEmpty(
                                        createCartItemAndCache(id)
                                )
                )
                .then();
    }

    private Mono<CartItem> updateCachedCount(Long id) {
        return redisCartItemRepository.getCachedObject(id)
                .flatMap(cartItem -> {
                    cartItem.setNew(cartItem.getCount() == 0);
                    cartItem.setCount(cartItem.getCount() + 1);
                    return cartItemRepository.save(cartItem).flatMap(redisCartItemRepository::cacheObject);
                });
    }

    private Mono<CartItem> updateCartItemCountAndCache(Long id) {
        return cartItemRepository.findById(id)
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() + 1);
                    return cartItemRepository.save(cartItem)
                            .flatMap(redisCartItemRepository::cacheObject);
                });
    }

    private Mono<CartItem> createCartItemAndCache(Long id) {
        return Mono.just(new CartItem(id, 1, true))
                .flatMap(cartItemRepository::save)
                .flatMap(redisCartItemRepository::cacheObject);
    }
}
