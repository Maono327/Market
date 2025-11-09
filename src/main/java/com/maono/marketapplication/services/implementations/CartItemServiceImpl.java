package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Override
    public void changeProductCountInTheCart(Long productId, ProductActionType actionType) {
        CartItem cartItem = cartItemRepository.findById(productId).orElse(null);
        switch (actionType) {
            case PLUS -> {
                if (cartItem == null) {
                    Product product = productService.findProductById(productId);
                    cartItem = new CartItem(product, 1);
                    product.setCartItem(cartItem);
                } else {
                    cartItem.incrementCount();
                }
                cartItemRepository.save(cartItem);
            }
            case MINUS -> {
                if (cartItem != null) {
                    cartItem.decrementCount();
                    if (cartItem.getCount() == 0) {
                        cartItem.getProduct().setCartItem(null);
                        cartItem.setProduct(null);
                        cartItemRepository.delete(cartItem);
                    } else {
                        cartItemRepository.save(cartItem);
                    }
                }
            }
            case DELETE -> {
                if (cartItem != null) {
                    cartItem.getProduct().setCartItem(null);
                    cartItem.setProduct(null);
                    cartItemRepository.delete(cartItem);
                }
            }
        }
    }

    @Override
    public List<CartItem> findAll() {
        return cartItemRepository.findAll();
    }

    @Override
    public void removeAll() {
        cartItemRepository.findAll().forEach(cartItem -> {
            cartItem.getProduct().setCartItem(null);
            cartItem.setProduct(null);
        });
        cartItemRepository.deleteAll();
    }

    @Override
    public BigDecimal calculateTotalSum(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
