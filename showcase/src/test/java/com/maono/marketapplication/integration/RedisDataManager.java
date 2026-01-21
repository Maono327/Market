package com.maono.marketapplication.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.util.CartItemCache;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.redis.util.ProductCache;
import com.maono.marketapplication.repositories.util.Page;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class RedisDataManager {
    private final ReactiveRedisTemplate<String, PageCache> pageRedisTemplate;
    private final ReactiveRedisTemplate<String, Integer> totalCountRedisTemplate;
    private final ReactiveRedisTemplate<String, ProductCache> productRedisTemplate;
    private final ReactiveRedisTemplate<String , CartItemCache> cartItemRedisTemplate;

    public RedisDataManager(ReactiveRedisTemplate<String, PageCache> pageRedisTemplate, ReactiveRedisTemplate<String, Integer> totalCountRedisTemplate, ReactiveRedisTemplate<String, ProductCache> productRedisTemplate, ReactiveRedisTemplate<String, CartItemCache> cartItemRedisTemplate) {
        this.pageRedisTemplate = pageRedisTemplate;
        this.totalCountRedisTemplate = totalCountRedisTemplate;
        this.productRedisTemplate = productRedisTemplate;
        this.cartItemRedisTemplate = cartItemRedisTemplate;
    }

    public void clear() {
        productRedisTemplate.execute(connection -> connection.serverCommands()
                .flushAll()).then().block();
    }

    public void cacheProduct(Product product) {
        productRedisTemplate.opsForValue().set(getProductKey(product.getId()), getProductCache(product)).block();
    }

    public void cacheCartItem(CartItem cartItem) {
        cartItemRedisTemplate.opsForValue().set(getCartItemKey(cartItem.getId()), getCartItemCache(cartItem)).block();
    }

    public void cachePage(Page<Product> page, int pageNumber, int pageSize, String search, String sortType) {
        pageRedisTemplate.opsForValue().set(getPageKey(pageNumber, pageSize, search, sortType), getPageCache(page)).block();
    }

    public Mono<PageCache> getPage(int pageNumber, int pageSize, String search, String sortType) {
        return pageRedisTemplate.opsForValue().get(getPageKey(pageNumber, pageSize, search, sortType));
    }

    public Mono<CartItemCache> getCartItemCache(Long id) {
        return cartItemRedisTemplate.opsForValue().get(getCartItemKey(id));
    }

    public Mono<ProductCache> getProductCache(Long id) {
        return productRedisTemplate.opsForValue().get(getProductKey(id));
    }

    private String getProductKey(Long id) {
        return "product:" + id;
    }

    private String getCartItemKey(Long id) {
        return "cartitem:" + id;
    }

    private CartItemCache getCartItemCache(CartItem cartItem) {
        return new CartItemCache(
                cartItem.getId(),
                cartItem.getCount()
        );
    }

    private ProductCache getProductCache(Product product) {
        return new ProductCache(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getImageName(),
                product.getPrice()
        );
    }

    private PageCache getPageCache(Page<Product> page) {
        return new PageCache(
                page.items(),
                page.pageSize(),
                page.pageNumber(),
                page.hasNext(),
                page.hasPrevious(),
                page.totalPages()
        );
    }

    @SneakyThrows
    private String getPageKey(int pageNumber, int pageSize, String search, String sortType) {
        Map<String, Object> payload = Map.of(
                "pageNumber", pageNumber,
                "pageSize", pageSize,
                "sortType", sortType,
                "search", search
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonKey = objectMapper.writeValueAsString(payload);
        return "page:" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(jsonKey.getBytes(StandardCharsets.UTF_8));
    }
}
