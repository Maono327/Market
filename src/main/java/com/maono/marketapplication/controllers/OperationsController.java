package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/buy")
@RequiredArgsConstructor
public class OperationsController {
    private final OrderService orderService;

    @PostMapping
    public String createOrder() {
        Order order = orderService.buy();
        return "redirect:/orders/" + order.getId() + "?newOrder=true";
    }
}
