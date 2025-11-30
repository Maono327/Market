package com.maono.marketapplication.services.util;

import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DecrementAction implements Strategy{
    private final CartItemRepository cartItemRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.MINUS;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return cartItemRepository.findById(id)
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() - 1);
                    if (cartItem.getCount() == 0) {
                        return cartItemRepository.delete(cartItem);
                    } else {
                        cartItem.setNew(false);
                        return cartItemRepository.save(cartItem);
                    }
                })
                .then();
    }
}
