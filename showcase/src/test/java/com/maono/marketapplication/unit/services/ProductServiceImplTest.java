package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.reactive.ProductRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisPageRepository;
import com.maono.marketapplication.repositories.redis.RedisProductRepository;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.services.implementations.ProductServiceImpl;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.page;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class})
public class ProductServiceImplTest {
    @Autowired
    protected ProductServiceImpl productService;
    @MockitoBean
    protected ProductRepository productRepository;
    @MockitoBean
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected RedisProductRepository redisProductRepository;
    @MockitoBean
    protected RedisCartItemRepository redisCartItemRepository;
    @MockitoBean
    protected RedisPageRepository redisPageRepository;

    @Test
    public void test_findByPage_pageCached_cartItemsCached() {
        String search = "";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        List<Product> cachedProducts = List.of(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(3L).get(),
                productByIdTemplate(4L).get(),
                productByIdTemplate(5L).get()
        );
        List<CartItem> cartItemsFromDb = List.of(
                cartItem(1L, 0).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get(),
                cartItem(4L, 3).get(),
                cartItem(5L, 1).get()
        );

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        PageCache cache = new PageCache(
                cachedProducts,
                pageSize,
                pageNumber,
                true,
                true,
                3
        );

        when(redisPageRepository.getCachedPage(
                "",
                5,
                2,
                ""
        )).thenReturn(Mono.just(cache));

        when(productRepository.findProductsByPage(5, 5, "")).thenReturn(Flux.empty());

        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(Flux.fromIterable(cartItemsFromDb));

        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(page -> {
                    assertEquals(expectedPage, page);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("", 5, 2, "");
        verify(productRepository).findProductsByPage(5, 5, "");
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verifyNoMoreInteractions(redisPageRepository, redisCartItemRepository, productRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    public void test_findByPage_pageCached_CartItemsCached_search() {
        String search = "abc";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        List<Product> cachedProducts = List.of(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(3L).get(),
                productByIdTemplate(4L).get(),
                productByIdTemplate(5L).get()
        );
        List<CartItem> cartItemsFromDb = List.of(
                cartItem(1L, 0).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get(),
                cartItem(4L, 3).get(),
                cartItem(5L, 1).get()
        );

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        PageCache cache = new PageCache(
                cachedProducts,
                pageSize,
                pageNumber,
                true,
                true,
                3
        );

        when(redisPageRepository.getCachedPage(
                "abc",
                5,
                2,
                ""
        )).thenReturn(Mono.just(cache));

        when(productRepository.findProductsByPageAndTitle("abc", 5, 5, ""))
                .thenReturn(Flux.empty());

        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(Flux.fromIterable(cartItemsFromDb));

        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(page -> {
                    assertEquals(expectedPage, page);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("abc", 5, 2, "");
        verify(productRepository).findProductsByPageAndTitle("abc", 5, 5, "");
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verifyNoMoreInteractions(redisPageRepository, redisCartItemRepository, productRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    public void test_findByPage_pageCached_CartItemPartly() {
        String search = "";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        List<Product> cachedProducts = List.of(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(3L).get(),
                productByIdTemplate(4L).get(),
                productByIdTemplate(5L).get()
        );
        List<CartItem> cartItemsFromRedisDb = List.of(
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get()
        );
        CartItem notExistentCartItem = cartItem(1L, 0).get();
        CartItem cartItem4FromDb = cartItem(4L, 3).get();
        CartItem cartItem5FromDb = cartItem(5L, 1).get();

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        PageCache cache = new PageCache(
                cachedProducts,
                pageSize,
                pageNumber,
                true,
                true,
                3
        );

        when(redisPageRepository.getCachedPage(
                "",
                5,
                2,
                ""
        )).thenReturn(Mono.just(cache));

        when(productRepository.findProductsByPage(5, 5, "")).thenReturn(Flux.empty());

        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(Flux.fromIterable(cartItemsFromRedisDb));

        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.findById(4L)).thenReturn(Mono.just(cartItem4FromDb));
        when(cartItemRepository.findById(5L)).thenReturn(Mono.just(cartItem5FromDb));

        when(redisCartItemRepository.cacheObject(notExistentCartItem)).thenReturn(Mono.just(notExistentCartItem));
        when(redisCartItemRepository.cacheObject(cartItem4FromDb)).thenReturn(Mono.just(cartItem4FromDb));
        when(redisCartItemRepository.cacheObject(cartItem5FromDb)).thenReturn(Mono.just(cartItem5FromDb));


        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(page -> {
                    assertEquals(expectedPage, page);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("", 5, 2, "");
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verify(cartItemRepository).findById(1L);
        verify(cartItemRepository).findById(4L);
        verify(cartItemRepository).findById(5L);
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 1L) && item.getCount() == 0));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 4L) && item.getCount() == 3));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 5L) && item.getCount() == 1));
        verify(productRepository).findProductsByPage(5, 5, "");

        verifyNoMoreInteractions(redisPageRepository, redisCartItemRepository, productRepository, cartItemRepository);
    }

    @Test
    public void test_findByPage_pageCached_CartItemsNotExist() {
        String search = "";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        List<Product> cachedProducts = List.of(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(3L).get(),
                productByIdTemplate(4L).get(),
                productByIdTemplate(5L).get()
        );

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).get(),
                        productByIdTemplate(5L).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        PageCache cache = new PageCache(
                cachedProducts,
                pageSize,
                pageNumber,
                true,
                true,
                3
        );
        CartItem notExistentCartItem1 = cartItem(1L, 0).get();
        CartItem notExistentCartItem2 = cartItem(2L, 0).get();
        CartItem notExistentCartItem3 = cartItem(3L, 0).get();
        CartItem notExistentCartItem4 = cartItem(4L, 0).get();
        CartItem notExistentCartItem5 = cartItem(5L, 0).get();

        when(redisPageRepository.getCachedPage(
                "",
                5,
                2,
                ""
        )).thenReturn(Mono.just(cache));

        when(productRepository.findProductsByPage(5, 5, "")).thenReturn(Flux.empty());

        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(Flux.empty());

        when(cartItemRepository.findById(anyLong())).thenReturn(Mono.empty());

        when(redisCartItemRepository.cacheObject(notExistentCartItem1)).thenReturn(Mono.just(notExistentCartItem1));
        when(redisCartItemRepository.cacheObject(notExistentCartItem2)).thenReturn(Mono.just(notExistentCartItem2));
        when(redisCartItemRepository.cacheObject(notExistentCartItem3)).thenReturn(Mono.just(notExistentCartItem3));
        when(redisCartItemRepository.cacheObject(notExistentCartItem4)).thenReturn(Mono.just(notExistentCartItem4));
        when(redisCartItemRepository.cacheObject(notExistentCartItem5)).thenReturn(Mono.just(notExistentCartItem5));


        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(page -> {
                    assertEquals(expectedPage, page);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("", 5, 2, "");
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verify(cartItemRepository, times(5)).findById(anyLong());
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 1L) && item.getCount() == 0));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 2L) && item.getCount() == 0));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 3L) && item.getCount() == 0));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 4L) && item.getCount() == 0));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 5L) && item.getCount() == 0));

        verify(productRepository).findProductsByPage(5, 5, "");

        verifyNoMoreInteractions(redisPageRepository, redisCartItemRepository, productRepository, cartItemRepository);

    }

    @Test
    public void test_findByPage_pageNotCached_cartItemsCached_totalCountNotCached() {
        String search = "";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        Product product1 = productByIdTemplate(1L).get();
        Product product2 = productByIdTemplate(2L).get();
        Product product3 = productByIdTemplate(3L).get();
        Product product4 = productByIdTemplate(4L).get();
        Product product5 = productByIdTemplate(5L).get();

        List<Product> productsFromDb = List.of(
                product1,
                product2,
                product3,
                product4,
                product5
        );

        List<CartItem> cartItemsFromDb = List.of(
                cartItem(1L, 0).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get(),
                cartItem(4L, 3).get(),
                cartItem(5L, 1).get()
        );

        Page<Product> page = page(productsFromDb, 5, 2, true, true, 3);

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        int totalCount = 15;

        when(redisPageRepository.getCachedPage("", 5, 2, ""))
                .thenReturn(Mono.empty());
        when(productRepository.findProductsByPage(5, 5, "")).thenReturn(Flux.fromIterable(productsFromDb));
        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(Flux.fromIterable(cartItemsFromDb));
        when(redisProductRepository.cacheObject(product1)).thenReturn(Mono.just(product1));
        when(redisProductRepository.cacheObject(product2)).thenReturn(Mono.just(product2));
        when(redisProductRepository.cacheObject(product3)).thenReturn(Mono.just(product3));
        when(redisProductRepository.cacheObject(product4)).thenReturn(Mono.just(product4));
        when(redisProductRepository.cacheObject(product5)).thenReturn(Mono.just(product5));
        when(redisProductRepository.getCachedTotalCount("")).thenReturn(Mono.empty());
        when(productRepository.totalCount()).thenReturn(Mono.just(totalCount));
        when(redisProductRepository.cacheTotalCount(totalCount, "")).thenReturn(Mono.just(totalCount));
        when(redisPageRepository.cachePage(page, 3, 2, 5, "", ""))
                .thenReturn(Mono.just(page));


        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(result -> {
                    assertEquals(expectedPage, result);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("", 5, 2, "");
        verify(productRepository).findProductsByPage(5, 5, "");
        verify(redisProductRepository).cacheObject(product1);
        verify(redisProductRepository).cacheObject(product2);
        verify(redisProductRepository).cacheObject(product3);
        verify(redisProductRepository).cacheObject(product4);
        verify(redisProductRepository).cacheObject(product5);
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verify(redisProductRepository).getCachedTotalCount("");
        verify(productRepository).totalCount();
        verify(redisProductRepository).cacheTotalCount(totalCount, "");
        verify(redisPageRepository).cachePage(page,3, 2, 5, "", "");

        verifyNoMoreInteractions(
                redisPageRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    public void test_findByPage_pageNotCached_cartItemsCached_totalCountCached() {
        String search = "";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        Product product1 = productByIdTemplate(1L).get();
        Product product2 = productByIdTemplate(2L).get();
        Product product3 = productByIdTemplate(3L).get();
        Product product4 = productByIdTemplate(4L).get();
        Product product5 = productByIdTemplate(5L).get();

        List<Product> productsFromDb = List.of(
                product1,
                product2,
                product3,
                product4,
                product5
        );

        List<CartItem> cartItemsFromDb = List.of(
                cartItem(1L, 0).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get(),
                cartItem(4L, 3).get(),
                cartItem(5L, 1).get()
        );

        Page<Product> page = page(productsFromDb, 5, 2, true, true, 3);

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        int totalCount = 15;

        when(redisPageRepository.getCachedPage("", 5, 2, ""))
                .thenReturn(Mono.empty());
        when(productRepository.findProductsByPage(5, 5, "")).thenReturn(Flux.fromIterable(productsFromDb));
        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(Flux.fromIterable(cartItemsFromDb));
        when(redisProductRepository.cacheObject(product1)).thenReturn(Mono.just(product1));
        when(redisProductRepository.cacheObject(product2)).thenReturn(Mono.just(product2));
        when(redisProductRepository.cacheObject(product3)).thenReturn(Mono.just(product3));
        when(redisProductRepository.cacheObject(product4)).thenReturn(Mono.just(product4));
        when(redisProductRepository.cacheObject(product5)).thenReturn(Mono.just(product5));
        when(redisProductRepository.getCachedTotalCount("")).thenReturn(Mono.just(totalCount));
        when(productRepository.totalCount()).thenReturn(Mono.empty());
        when(redisPageRepository.cachePage(page, 3, 2, 5, "", ""))
                .thenReturn(Mono.just(page));


        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(result -> {
                    assertEquals(expectedPage, result);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("", 5, 2, "");
        verify(productRepository).findProductsByPage(5, 5, "");
        verify(redisProductRepository).cacheObject(product1);
        verify(redisProductRepository).cacheObject(product2);
        verify(redisProductRepository).cacheObject(product3);
        verify(redisProductRepository).cacheObject(product4);
        verify(redisProductRepository).cacheObject(product5);
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verify(redisProductRepository).getCachedTotalCount("");
        verify(productRepository).totalCount();
        verify(redisPageRepository).cachePage(page,3, 2, 5, "", "");

        verifyNoMoreInteractions(
                redisPageRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    public void test_findByPage_pageNotCached_cartItemCached_totalCountCached_search() {
        String search = "abc";
        ProductSortType sortType = ProductSortType.NO;
        int pageSize = 5;
        int pageNumber = 2;

        Product product1 = productByIdTemplate(1L).get();
        Product product2 = productByIdTemplate(2L).get();
        Product product3 = productByIdTemplate(3L).get();
        Product product4 = productByIdTemplate(4L).get();
        Product product5 = productByIdTemplate(5L).get();

        List<Product> productsFromDb = List.of(
                product1,
                product2,
                product3,
                product4,
                product5
        );

        List<CartItem> cartItemsFromDb = List.of(
                cartItem(1L, 0).get(),
                cartItem(2L, 2).get(),
                cartItem(3L, 0).get(),
                cartItem(4L, 3).get(),
                cartItem(5L, 1).get()
        );

        Page<Product> page = page(productsFromDb, 5, 2, true, true, 3);

        Page<Product> expectedPage = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).withCartItemByCount(2).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).withCartItemByCount(3).get(),
                        productByIdTemplate(5L).withCartItemByCount(1).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        int totalCount = 15;

        when(redisPageRepository.getCachedPage("abc", 5, 2, ""))
                .thenReturn(Mono.empty());
        when(productRepository.findProductsByPageAndTitle("abc", 5, 5, ""))
                .thenReturn(Flux.fromIterable(productsFromDb));
        when(redisProductRepository.cacheObject(product1)).thenReturn(Mono.just(product1));
        when(redisProductRepository.cacheObject(product2)).thenReturn(Mono.just(product2));
        when(redisProductRepository.cacheObject(product3)).thenReturn(Mono.just(product3));
        when(redisProductRepository.cacheObject(product4)).thenReturn(Mono.just(product4));
        when(redisProductRepository.cacheObject(product5)).thenReturn(Mono.just(product5));
        when(redisCartItemRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(Flux.fromIterable(cartItemsFromDb));
        when(redisProductRepository.getCachedTotalCount("abc")).thenReturn(Mono.just(totalCount));
        when(productRepository.totalCountBySearch("abc")).thenReturn(Mono.empty());
        when(redisPageRepository.cachePage(page, 3, 2, 5, "abc", ""))
                .thenReturn(Mono.just(page));


        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(result -> {
                    assertEquals(expectedPage, result);
                })
                .verifyComplete();

        verify(redisPageRepository).getCachedPage("abc", 5, 2, "");
        verify(productRepository).findProductsByPageAndTitle("abc", 5, 5, "");
        verify(redisProductRepository).cacheObject(product1);
        verify(redisProductRepository).cacheObject(product2);
        verify(redisProductRepository).cacheObject(product3);
        verify(redisProductRepository).cacheObject(product4);
        verify(redisProductRepository).cacheObject(product5);
        verify(redisCartItemRepository).multiGet(List.of(1L, 2L, 3L, 4L, 5L));
        verify(redisProductRepository).getCachedTotalCount("abc");
        verify(productRepository).totalCountBySearch("abc");
        verify(redisPageRepository).cachePage(page,3, 2, 5, "abc", "");

        verifyNoMoreInteractions(
                redisPageRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    public void test_findProductByIdWithRelations_allCached() {
        Product cachedProduct = productByIdTemplate(1L).get();

        when(redisProductRepository.getCachedObject(1L)).thenReturn(Mono.just(cachedProduct));
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        CartItem cartItem = cartItem(1L, 2).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());

        Product expected = productByIdTemplate(1L).withCartItemByCount(2).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    assertEquals(expected, product);
                })
                .verifyComplete();

        verify(redisProductRepository).getCachedObject(1L);
        verify(productRepository).findById(1L);

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);

        verifyNoMoreInteractions(
                cartItemRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(redisPageRepository);
    }

    @Test
    public void test_findProductByIdWithRelations_cartItemNotCached() {
        Product cachedProduct = productByIdTemplate(1L).get();

        when(redisProductRepository.getCachedObject(1L)).thenReturn(Mono.just(cachedProduct));
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        CartItem cartItem = cartItem(1L, 2).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItem));
        when(redisCartItemRepository.cacheObject(cartItem)).thenReturn(Mono.just(cartItem));

        Product expected = productByIdTemplate(1L).withCartItemByCount(2).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    assertEquals(expected, product);
                })
                .verifyComplete();

        verify(redisProductRepository).getCachedObject(1L);
        verify(productRepository).findById(1L);

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository).cacheObject(cartItem);


        verifyNoMoreInteractions(
                cartItemRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(redisPageRepository);
    }

    @Test
    public void test_findProductByIdWithRelations_cartItemNotExists() {
        Product cachedProduct = productByIdTemplate(1L).get();

        when(redisProductRepository.getCachedObject(1L)).thenReturn(Mono.just(cachedProduct));
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        CartItem notExistCartItem = cartItem(1L, 0).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());
        when(redisCartItemRepository.cacheObject(notExistCartItem)).thenReturn(Mono.just(notExistCartItem));

        Product expected = productByIdTemplate(1L).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    assertEquals(expected, product);
                })
                .verifyComplete();

        verify(redisProductRepository).getCachedObject(1L);
        verify(productRepository).findById(1L);

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);
        verify(redisCartItemRepository).cacheObject(cartItem(1L, 0).get());

        verifyNoMoreInteractions(
                cartItemRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(redisPageRepository);
    }

    @Test
    public void test_test_findProductByIdWithRelations_productNotCached() {
        Product product = productByIdTemplate(1L).get();

        when(redisProductRepository.getCachedObject(1L)).thenReturn(Mono.empty());
        when(productRepository.findById(1L)).thenReturn(Mono.just(product));
        when(redisProductRepository.cacheObject(product)).thenReturn(Mono.just(product));

        CartItem cachedCartItem = cartItem(1L, 2).get();
        when(redisCartItemRepository.getCachedObject(1L)).thenReturn(Mono.just(cachedCartItem));
        when(cartItemRepository.findById(1L)).thenReturn(Mono.empty());

        Product expected = productByIdTemplate(1L).withCartItemByCount(2).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(p -> {
                    assertEquals(expected, p);
                })
                .verifyComplete();

        verify(redisProductRepository).getCachedObject(1L);
        verify(productRepository).findById(1L);
        verify(redisProductRepository).cacheObject(productByIdTemplate(1L).get());

        verify(redisCartItemRepository).getCachedObject(1L);
        verify(cartItemRepository).findById(1L);

        verifyNoMoreInteractions(
                cartItemRepository,
                productRepository,
                redisProductRepository,
                redisCartItemRepository
        );
        verifyNoInteractions(redisPageRepository);
    }
}
