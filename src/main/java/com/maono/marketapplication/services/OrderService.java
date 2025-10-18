package com.maono.marketapplication.services;

import com.maono.marketapplication.models.Order;

import java.util.List;

public interface OrderService {
    List<Order> findAll();
    Order findById(Long id);
    Order buy();
}
