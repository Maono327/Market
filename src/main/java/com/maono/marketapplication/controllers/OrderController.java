package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.mappers.OrderDtoMapper;
import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getOrders() {
        return orderService.findAllWithRelations()
                .collectList()
                .map(orders -> Rendering.view("product_orders")
                        .modelAttribute("orders", orders.stream().map(OrderDtoMapper::mapToOrderDto).toList())
                        .build());
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getOder(@PathVariable Long id,
                                   @RequestParam(required = false, defaultValue = "false") boolean newOrder) {
        return orderService.findByIdWithRelations(id)
                .map(order -> Rendering.view("product_order")
                        .modelAttribute("order", OrderDtoMapper.mapToOrderDto(order))
                        .modelAttribute("newOrder", newOrder)
                        .build());
    }

}
