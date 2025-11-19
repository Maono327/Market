package com.maono.marketapplication.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;

import java.util.ArrayList;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;

public class ExpectedCartItemTestDataProvider {

    public static class StagedCartItemTestDataBuilder {
        private final CartItem cartItem;

        private StagedCartItemTestDataBuilder(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public StagedCartItemTestDataBuilder withProductByTemplate() {
            Product product = productByIdTemplate(cartItem.getId()).get();
            cartItem.setProduct(product);
            product.setCartItem(cartItem);
            return this;
        }

        public StagedCartItemTestDataBuilder withProduct(Product product) {
            cartItem.setProduct(product);
            product.setCartItem(cartItem);
            return this;
        }

        public CartItem get() {
            return cartItem;
        }
    }

    public static StagedCartItemTestDataBuilder cartItem(Long id, int count) {
        CartItem c = CartItem.builder()
                .id(id)
                .count(count)
                .build();

        return new StagedCartItemTestDataBuilder(c);
    }

    public static List<CartItem> cartItemList(List<Integer> counts) {
        List<CartItem> cartItemList = new ArrayList<>();
        for (int i = 0; i < counts.size(); i++) {
            CartItem c = cartItem(1L + i, counts.get(i)).withProductByTemplate().get();
            cartItemList.add(c);
        }
        return cartItemList;
    }
}
