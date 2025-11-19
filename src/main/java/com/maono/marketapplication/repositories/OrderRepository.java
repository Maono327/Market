package com.maono.marketapplication.repositories;

import com.maono.marketapplication.models.Order;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository {
    Flux<Order> findAllWithRelations();
    Mono<Order> findByIdWithRelations(Long id);
    Mono<Order> save(Order order);
}
