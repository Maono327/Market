package com.maono.marketapplication.controllers;

import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/buy")
@RequiredArgsConstructor
public class OperationsController {
    private final OrderService orderService;

    @PostMapping
    public Mono<Rendering> createOrder() {
        return orderService.buy()
                .map(order -> Rendering.redirectTo("/orders/" + order.getId() + "?newOrder=true").build());
    }
}
