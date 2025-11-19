package com.maono.marketapplication.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.util.ProductActionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemService {
    Mono<Void> changeProductCountInTheCart(Long productId, ProductActionType actionType);
    Flux<CartItem> findAllWithRelations();
    Mono<Void> removeAll();
    BigDecimal calculateTotalSum(List<CartItem> cartItems);
}
