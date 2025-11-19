package com.maono.marketapplication.repositories;

import com.maono.marketapplication.models.OrderItem;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderItemRepository {
    Mono<Void> saveOrderItems(List<OrderItem> orderItems);
}
