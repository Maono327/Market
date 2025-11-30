package com.maono.marketapplication.services.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class IncrementAction implements Strategy {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.PLUS;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return cartItemRepository.findById(id)
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() + 1);
                    cartItem.setNew(false);
                    return cartItemRepository.save(cartItem);
                })
                .switchIfEmpty(productRepository.findById(id)
                                    .flatMap(product -> {
                                        CartItem cartItem = new CartItem(product, 1, true);
                                        return cartItemRepository.save(cartItem);
                                    }))
                .then();
    }
}
