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
public class DecrementAction implements Strategy{
    private final CartItemRepository cartItemRepository;
    private final RedisCartItemRepository redisCartItemRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.MINUS;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return redisCartItemRepository.getCachedObject(id)
                .switchIfEmpty(
                        cartItemRepository.findById(id)
                                .switchIfEmpty(
                                        redisCartItemRepository.cacheObject(new CartItem(id, 0))
                                        .then(Mono.empty())
                                )
                )
                .flatMap(cartItem -> {
                    if (cartItem.getCount() == 0) {
                        return Mono.empty();
                    }
                    cartItem.setCount(cartItem.getCount() - 1);
                    if (cartItem.getCount() == 0) {
                        cartItem.setCount(0);
                        return redisCartItemRepository.cacheObject(cartItem).then(cartItemRepository.delete(cartItem));
                    } else {
                        cartItem.setNew(false);
                        return cartItemRepository.save(cartItem).flatMap(redisCartItemRepository::cacheObject);
                    }
                }).then();
    }
}
