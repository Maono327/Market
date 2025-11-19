package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.repositories.OrderItemRepository;
import com.maono.marketapplication.repositories.OrderRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartItemService cartItemService;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Flux<Order> findAllWithRelations() {
        return orderRepository.findAllWithRelations();
    }

    @Override
    public Mono<Order> findByIdWithRelations(Long id) {
        return orderRepository.findByIdWithRelations(id);
    }

    @Override
    @Transactional
    public Mono<Order> buy() {
        return cartItemService.findAllWithRelations()
                .collectList()
                .flatMap(cartItems -> {
                    Order order = new Order();
                    order.setTotalSum(cartItemService.calculateTotalSum(cartItems));
                    return orderRepository.save(order)
                            .flatMap(saved -> {
                                List<OrderItem> orderItems = cartItems.stream()
                                        .map(cartItem -> OrderItem.builder()
                                                .orderId(saved.getId())
                                                .order(saved)
                                                .count(cartItem.getCount())
                                                .product(cartItem.getProduct())
                                                .productId(cartItem.getProduct().getId())
                                                .build())
                                        .toList();
                                return orderItemRepository.saveOrderItems(orderItems)
                                        .then(cartItemService.removeAll())
                                        .then(Mono.just(saved));
                            });
                });
    }
}
