package com.maono.marketapplication.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.List;

public class ExpectedCartItemTestDataProvider {
    public static List<CartItem> buildCartItemsList(Pair<Long, Integer>... items) {
        return Arrays.stream(items)
                .map(item -> buildCartItemByProductId(item.getFirst(), item.getSecond())).toList();
    }

    public static CartItem buildCartItemByProductId(Long productId, int count) {
        Product product = ExpectedProductsTestDataProvider.buildProductById(productId);

        CartItem cartItem = CartItem.builder()
            .id(productId)
            .product(product)
            .count(count)
            .build();
        product.setCartItem(cartItem);
        return cartItem;
    }

    public static CartItem buildCartItemByProduct(Product product, int count) {
        CartItem cartItem = CartItem.builder()
                .id(product.getId())
                .product(product)
                .count(count)
                .build();
        product.setCartItem(cartItem);
        return cartItem;
    }

    public static CartItem buildCartItemWithManagedEntity(Product product, int count) {
        return CartItem.builder()
                .product(product)
                .count(count)
                .build();
    }

}
