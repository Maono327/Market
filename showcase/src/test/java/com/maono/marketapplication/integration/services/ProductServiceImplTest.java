package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.RedisDataManager;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static org.springframework.data.relational.core.query.Query.query;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class ProductServiceImplTest {
    @Autowired
    protected ProductService productService;
    @Autowired
    protected RedisDataManager redisDataManager;
    @Autowired
    protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private ResetDataManager resetDataManager;

    @AfterEach
    public void cleanUp() {
        redisDataManager.clear();
    }

    @Test
    public void test_findByPage_pageCached() {
        Page<Product> cachedPage = page(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        redisDataManager.cachePage(cachedPage, 1, 5, "abc", "");

        Page<Product> expectedPagge = page(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("abc", ProductSortType.NO, 5, 1))
                .assertNext(page -> assertEquals(expectedPagge, page))
                .verifyComplete();
    }

    @Test
    public void test_findByPage_AllCachedPartly() {
        assertNull(redisDataManager.getPage(1, 5, "", "").block());

        Product cachedBriefcase = briefcaseProduct().get();
        Product cachedUmbrella = umbrellaProduct().get();
        Product cachedVase = vaseProduct().get();

        redisDataManager.cacheProduct(cachedBriefcase);
        redisDataManager.cacheProduct(cachedUmbrella);
        redisDataManager.cacheProduct(cachedVase);

        CartItem cachedBookCartItem = cartItem(1L, 3).get();
        CartItem cachedUmbrellaCartItem = cartItem(4L, 0).get();

        redisDataManager.cacheCartItem(cachedBookCartItem);
        redisDataManager.cacheCartItem(cachedUmbrellaCartItem);

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        Page<Product> expected = page(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("", ProductSortType.NO, 5, 1))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream()
                            .map(product -> {
                                CartItem cartItem = product.getCartItem();
                                if (cartItem == null) {
                                    cartItem = cartItem(product.getId(), 0).withProduct(product).get();
                                }
                                return cartItem;
                            })
                            .toList();
                    List<CartItem> pageCartItems = productPage.items().stream()
                            .map(Product::getCartItem)
                            .toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(3, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(1)
                .verifyComplete();

        PageCache expectedPageCache = new PageCache(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(redisDataManager.getPage(1, 5, "", ""))
                .assertNext(page -> assertEquals(expectedPageCache, page))
                .verifyComplete();
    }

    @Test
    public void test_findByPage_AllNotCached() {
        assertNull(redisDataManager.getPage(1, 5, "", "").block());

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();


        Page<Product> expected = page(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("", ProductSortType.NO, 5, 1))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream()
                            .map(product -> {
                                CartItem cartItem = product.getCartItem();
                                if (cartItem == null) {
                                    cartItem = cartItem(product.getId(), 0).withProduct(product).get();
                                }
                                return cartItem;
                            })
                            .toList();
                    List<CartItem> pageCartItems = productPage.items().stream()
                            .map(Product::getCartItem)
                            .toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(3, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(1)
                .verifyComplete();

        PageCache expectedPageCache = new PageCache(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        umbrellaProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(redisDataManager.getPage(1, 5, "", ""))
                .assertNext(page -> assertEquals(expectedPageCache, page))
                .verifyComplete();
    }

    @Test
    public void test_findByPage_search_AllNotCached() {
        assertNull(redisDataManager.getPage(1, 5, "о", "").block());

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();


        Page<Product> expected = page(
                List.of(briefcaseProduct().withCartItemByCount(1).get(),
                        umbrellaProduct().get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("о", ProductSortType.NO, 5, 1))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream()
                            .map(product -> {
                                CartItem cartItem = product.getCartItem();
                                if (cartItem == null) {
                                    cartItem = cartItem(product.getId(), 0).withProduct(product).get();
                                }
                                return cartItem;
                            })
                            .toList();
                    List<CartItem> pageCartItems = productPage.items().stream()
                            .map(Product::getCartItem)
                            .toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        PageCache expectedPageCache = new PageCache(
                List.of(briefcaseProduct().withCartItemByCount(1).get(),
                        umbrellaProduct().get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(redisDataManager.getPage(1, 5, "о", ""))
                .assertNext(page -> assertEquals(expectedPageCache, page))
                .verifyComplete();
    }

    @Test
    public void test_findByPage_sort_alpha_allNotCached() {
        assertNull(redisDataManager.getPage(1, 5, "", "title").block());

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .expectNextCount(0)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .expectNextCount(0)
                .verifyComplete();

        Page<Product> expected = page(
                List.of(polaroidProduct().get(),
                        vaseProduct().withCartItemByCount(2).get(),
                        umbrellaProduct().get(),
                        bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("", ProductSortType.ALPHA, 5, 1))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream()
                            .map(product -> {
                                CartItem cartItem = product.getCartItem();
                                if (cartItem == null) {
                                    cartItem = cartItem(product.getId(), 0).withProduct(product).get();
                                }
                                return cartItem;
                            })
                            .toList();
                    List<CartItem> pageCartItems = productPage.items().stream()
                            .map(Product::getCartItem)
                            .toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .assertNext(cartItemCache -> assertEquals(3, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(2L))
                .assertNext(cartItemCache -> assertEquals(1, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(3L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(4L))
                .assertNext(cartItemCache -> assertEquals(0, cartItemCache.count()))
                .verifyComplete();
        StepVerifier.create(redisDataManager.getCartItemCache(5L))
                .assertNext(cartItemCache -> assertEquals(2, cartItemCache.count()))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(3L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(4L))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(redisDataManager.getProductCache(5L))
                .expectNextCount(1)
                .verifyComplete();

        PageCache expectedPageCache = new PageCache(
                List.of(polaroidProduct().get(),
                        vaseProduct().withCartItemByCount(2).get(),
                        umbrellaProduct().get(),
                        bookProduct().withCartItemByCount(3).get(),
                        briefcaseProduct().withCartItemByCount(1).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(redisDataManager.getPage(1, 5, "", "title"))
                .assertNext(page -> assertEquals(expectedPageCache, page))
                .verifyComplete();
    }

    @Test
    public void test_findProductByIdWithRelations_AllCached() {
        Product cachedProduct = bookProduct().get();
        redisDataManager.cacheProduct(cachedProduct);

        CartItem cachedCartItem = cartItem(1L, 3).get();
        redisDataManager.cacheCartItem(cachedCartItem);

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    Product expected = bookProduct().withCartItemByCount(3).get();
                    assertEquals(expected, product);
                    assertEquals(expected.getCartItem(), product.getCartItem());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_findProductByIdWithRelations_AllCachedPartly() {
        Product cachedProduct = bookProduct().get();
        redisDataManager.cacheProduct(cachedProduct);

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    Product expected = bookProduct().withCartItemByCount(3).get();
                    assertEquals(expected, product);
                    assertEquals(expected.getCartItem(), product.getCartItem());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_findProductByIdWithRelations_AllNotCached() {

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(0)
                .verifyComplete();


        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    Product expected = bookProduct().withCartItemByCount(3).get();
                    assertEquals(expected, product);
                    assertEquals(expected.getCartItem(), product.getCartItem());
                })
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(redisDataManager.getCartItemCache(1L))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_importProducts() {
        Product book = bookProduct().get();
        Product briefCase = briefcaseProduct().get();

        redisDataManager.cacheProduct(book);
        redisDataManager.cacheProduct(briefCase);

        Product import1 = Product.builder()
                .title("Test 1")
                .description("description 1")
                .imageName("test1.png")
                .price(new BigDecimal("100.00"))
                .build();

        Product import2 = Product.builder()
                .title("Test 2")
                .description("description 2")
                .imageName("test2.png")
                .price(new BigDecimal("200.00"))
                .build();

        List<Product> imports = List.of(import1, import2);

        StepVerifier.create(r2dbcEntityTemplate.select(Product.class).all())
                        .expectNextCount(5)
                        .verifyComplete();

        StepVerifier.create(productService.importProducts(imports, Flux.empty(), ""))
                        .expectNextCount(0)
                        .verifyComplete();


        Product expected1 = Product.builder()
                .id(6L)
                .title("Test 1")
                .description("description 1")
                .imageName("test1.png")
                .price(new BigDecimal("100.00"))
                .build();

        Product expected2 = Product.builder()
                .id(7L)
                .title("Test 2")
                .description("description 2")
                .imageName("test2.png")
                .price(new BigDecimal("200.00"))
                .build();

        StepVerifier.create(r2dbcEntityTemplate.select(Product.class)
                .matching(query(where("id").is(6L)))
                .one())
                .assertNext(product -> assertEquals(expected1, product))
                .verifyComplete();

        StepVerifier.create(r2dbcEntityTemplate.select(Product.class)
                        .matching(query(where("id").is(7L)))
                        .one())
                .assertNext(product -> assertEquals(expected2, product))
                .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(1L))
                        .expectNextCount(0)
                        .verifyComplete();

        StepVerifier.create(redisDataManager.getProductCache(2L))
                .expectNextCount(0)
                .verifyComplete();

        redisDataManager.clear();
        resetDataManager.resetAll();
    }
}
