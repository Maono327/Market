package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisPageRepository;
import com.maono.marketapplication.repositories.redis.RedisProductRepository;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.util.ProductSortType;
import com.maono.marketapplication.repositories.reactive.ProductRepository;
import com.maono.marketapplication.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final RedisProductRepository redisProductRepository;
    private final RedisCartItemRepository redisCartItemRepository;
    private final RedisPageRepository redisPageRepository;

    @Override
    public Mono<Page<Product>> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber) {

        String sortBy = switch (sort) {
            case ALPHA -> "title";
            case PRICE -> "price";
            case NO -> "";
        };

        return findPageCache(search, pageSize, pageNumber, sortBy)
                .flatMap(this::buildPageFromCache)
                .switchIfEmpty(
                        findProductsWithPaging(search, pageSize, pageNumber, sortBy)
                                .collectList()
                                .flatMap(this::attachCartItems)
                                .flatMap(products -> calculateTotalCount(search)
                                        .map(totalCount -> {
                                            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                                            return new Page<>(
                                                    products,
                                                    pageSize,
                                                    pageNumber,
                                                    pageNumber < totalPages,
                                                    pageNumber > 1,
                                                    totalPages);
                                            }
                                        )
                                )
                                .flatMap(page ->
                                        redisPageRepository.cachePage(
                                                page,
                                                page.totalPages(),
                                                pageNumber,
                                                pageSize,
                                                search,
                                                sortBy
                                        )
                                )
                );
    }

    @Override
    public Mono<Product> findProductByIdWithRelations(Long id) {
        return findProductById(id).flatMap(this::attachCartItem);
    }

    protected Mono<Product> findProductById(Long id) {
        return redisProductRepository.getCachedObject(id)
                .switchIfEmpty(
                        productRepository
                                .findById(id)
                                .flatMap(redisProductRepository::cacheObject)
                );
    }

    protected Mono<Product> attachCartItem(Product product) {
        return redisCartItemRepository.getCachedObject(product.getId())
                .switchIfEmpty(
                        cartItemRepository.findById(product.getId())
                                .switchIfEmpty(Mono.just(new CartItem(product.getId(), 0)))
                                .flatMap(redisCartItemRepository::cacheObject)
                )
                .map(cartItem -> {
                    product.setCartItem(cartItem);
                    cartItem.setProduct(product);
                    return product;
                });
    }

    protected Mono<Integer> calculateTotalCount(String search) {
        Mono<Integer> result = redisProductRepository.getCachedTotalCount(search);
        if (search.isBlank()) {
             return result.switchIfEmpty(
                    productRepository.totalCount()
                            .flatMap(totalCount -> redisProductRepository.cacheTotalCount(totalCount, search))
             );
        }
        else {
            return result.switchIfEmpty(
                    productRepository.totalCountBySearch(search)
                            .flatMap(totalCount -> redisProductRepository.cacheTotalCount(totalCount, search))
            );
        }
    }

    protected Flux<Product> findProductsWithPaging(String search, int pageSize, int pageNumber, String sortBy) {
        int offset = (pageNumber - 1) * pageSize;
        if (search.isBlank()) {
            return productRepository.findProductsByPage(pageSize, offset, sortBy)
                    .flatMap(redisProductRepository::cacheObject);
        } else {
            return productRepository.findProductsByPageAndTitle(search, pageSize, offset, sortBy)
                    .flatMap(redisProductRepository::cacheObject);
        }
    }

    protected Mono<PageCache> findPageCache(String search, int pageSize, int pageNumber, String sortBy) {
        return redisPageRepository.getCachedPage(search, pageSize, pageNumber, sortBy);
    }

    protected Mono<Page<Product>> buildPageFromCache(PageCache pageCache) {
        return Mono.just(pageCache.products())
                .flatMap(this::attachCartItems)
                .map(products -> new Page<>(
                        products,
                        pageCache.pageSize(),
                        pageCache.pageNumber(),
                        pageCache.hasNext(),
                        pageCache.hasPrevious(),
                        pageCache.totalPages())
                );
    }

    protected Mono<List<Product>> attachCartItems(List<Product> products) {
        List<Long> ids = products.stream().map(Product::getId).toList();
        Map<Long, Product> productMap = new HashMap<>();
        products.forEach(product -> productMap.put(product.getId(), product));

        Function<CartItem, CartItem> attachProductToCartItem = cartItem -> {
            cartItem.setProduct(productMap.get(cartItem.getId()));
            productMap.get(cartItem.getId()).setCartItem(cartItem);
            return cartItem;
        };

        return redisCartItemRepository.multiGet(ids)
                .collectList()
                .flatMap(cachedCartItems -> {
                    List<Long> missedCachedCartItemsIds = new ArrayList<>(ids);
                    missedCachedCartItemsIds.removeAll(cachedCartItems.stream().map(CartItem::getId).toList());

                    if (missedCachedCartItemsIds.isEmpty()) {
                        cachedCartItems.forEach(attachProductToCartItem::apply);
                        return Mono.just(products);
                    } else {
                        return Flux.fromIterable(missedCachedCartItemsIds)
                                .doOnNext(System.out::println)
                                .flatMap(id -> cartItemRepository.findById(id)
                                        .switchIfEmpty(Mono.just(new CartItem(id, 0)).doOnNext(i -> System.out.print("Создание пустого карт айтема: " + i))))
                                .doOnNext(i -> System.out.println("Кеширование " + i))
                                .flatMap(cartItem -> redisCartItemRepository.cacheObject(cartItem).doOnNext(c3 -> System.out.println("зак123ешировано " + c3)))
                                .collectList()
                                .map(cartItems -> {
                                    cartItems.addAll(cachedCartItems);
                                    cartItems.forEach(attachProductToCartItem::apply);
                                    return products;
                                });
                    }
                });
    }
}
