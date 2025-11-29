package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductActionStrategy productActionStrategy;
    private final ProductRepository productRepository;

    @Override
    public Mono<Void> changeProductCountInTheCart(Long productId, ProductActionType actionType) {
        return productActionStrategy.execute(actionType, productId);
    }

    @Override
    public Flux<CartItem> findAllWithRelations() {
        return cartItemRepository.findAll()
                .collectList()
                .flatMapMany(cartItems -> {
                    List<Long> productIds = cartItems.stream()
                            .map(CartItem::getId)
                            .toList();
                    return productRepository.findAllById(productIds)
                            .collectMap(Product::getId)
                            .flatMapMany(productMap ->
                                    Flux.fromIterable(cartItems)
                                    .map(item -> {
                                        Product product = productMap.get(item.getId());
                                        product.setCartItem(item);
                                        item.setProduct(product);
                                        return item;
                                    }));
                });
    }

    @Override
    public Mono<Void> removeAll() {
        return cartItemRepository.deleteAll();
    }

    @Override
    public BigDecimal calculateTotalSum(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
