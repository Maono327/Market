package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.mappers.OrderDtoMapper;
import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public String getOrders(Model model) {
        List<Order> orders = orderService.findAll();

        model.addAttribute("orders", orders.stream().map(OrderDtoMapper::mapToOrderDto).toList());

        return "product_orders";
    }

    @GetMapping("/{id}")
    public String getOder(Model model,
                          @PathVariable Long id,
                          @RequestParam(name = "newOrder", required = false, defaultValue = "false") boolean newOrder) {
        Order order = orderService.findById(id);

        model.addAttribute("order", OrderDtoMapper.mapToOrderDto(order));
        model.addAttribute("newOrder", newOrder);

        return "product_order";
    }

}
