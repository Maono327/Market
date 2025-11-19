package com.maono.marketapplication.services.util;

import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DeleteAction implements Strategy {
    private final CartItemRepository cartItemRepository;

    @Override
    public ProductActionType getType() {
        return ProductActionType.DELETE;
    }

    @Override
    public Mono<Void> executeChange(Long id) {
        return cartItemRepository.findById(id)
                .flatMap(cartItemRepository::delete);
    }
}
