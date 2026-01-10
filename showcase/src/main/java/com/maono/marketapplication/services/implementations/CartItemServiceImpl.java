package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.reactive.ProductRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisProductRepository;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductActionStrategy productActionStrategy;
    private final ProductRepository productRepository;
    private final RedisCartItemRepository redisCartItemRepository;
    private final RedisProductRepository redisProductRepository;

    @Override
    public Mono<Void> changeProductCountInTheCart(Long productId, ProductActionType actionType) {
        return productActionStrategy.execute(actionType, productId);
    }

    @Override
    public Flux<CartItem> findAllWithRelations() {
        return cartItemRepository.findAll()
                .flatMap(redisCartItemRepository::cacheObject)
                .collectList()
                .flatMapMany(this::attachProducts);
    }

    private Flux<CartItem> attachProducts(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return Flux.empty();
        }

        List<Long> cartItemsIds = cartItems.stream().map(CartItem::getId).toList();
        return redisProductRepository.multiGet(cartItemsIds)
                .collectList()
                .flatMapMany(loadedProducts -> {
                    List<Long> missed = new ArrayList<>(cartItemsIds);
                    missed.removeAll(loadedProducts.stream().map(Product::getId).toList());
                    if (missed.isEmpty()) {
                        Map<Long, Product> productMap = new HashMap<>();
                        loadedProducts.forEach(p -> productMap.put(p.getId(), p));

                        cartItems.forEach(c -> {
                            Product p = productMap.get(c.getId());
                            c.setProduct(p);
                            p.setCartItem(c);
                        });

                        return Flux.fromIterable(cartItems);
                    } else {
                        return productRepository.findAllById(missed)
                                .flatMap(redisProductRepository::cacheObject)
                                .collectList()
                                .flatMapMany(loadedProductsFromDb -> {
                                    Map<Long, Product> productMap = new HashMap<>();
                                    loadedProducts.forEach(p -> productMap.put(p.getId(), p));
                                    loadedProductsFromDb.forEach(p -> productMap.put(p.getId(), p));

                                    cartItems.forEach(c -> {
                                        Product p = productMap.get(c.getId());
                                        c.setProduct(p);
                                        p.setCartItem(c);
                                    });

                                    return Flux.fromIterable(cartItems);
                                });
                    }

                });
    }

    @Override
    public Mono<Void> removeAll() {
        return redisCartItemRepository.dropAllCounts().then(cartItemRepository.deleteAll());
    }

    @Override
    public BigDecimal calculateTotalSum(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
