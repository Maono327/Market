package com.maono.marketapplication.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.util.ProductActionType;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemService {
    void changeProductCountInTheCart(Long productId, ProductActionType actionType);
    List<CartItem> findAll();
    void removeAll();
    BigDecimal calculateTotalSum(List<CartItem> cartItems);
}
