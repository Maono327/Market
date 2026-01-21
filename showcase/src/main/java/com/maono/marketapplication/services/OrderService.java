package com.maono.marketapplication.services;

import com.maono.marketapplication.models.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<Order> findAllWithRelations();
    Mono<Order> findByIdWithRelations(Long id);
    Mono<Order> buy();
}
