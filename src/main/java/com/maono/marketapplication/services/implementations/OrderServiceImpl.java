package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.repositories.OrderRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartItemService cartItemService;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Order buy() {
        Order order = new Order();

        List<CartItem> cartItems = cartItemService.findAll();
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .count(cartItem.getCount())
                        .product(cartItem.getProduct())
                        .build()).toList();

        order.setItems(orderItems);

        BigDecimal totalSum = cartItemService.calculateTotalSum(cartItems);

        order.setTotalSum(totalSum);

        cartItemService.removeAll();
        return orderRepository.save(order);
    }
}
