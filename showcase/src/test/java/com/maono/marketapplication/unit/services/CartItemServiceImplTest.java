package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.reactive.CartItemRepository;
import com.maono.marketapplication.repositories.reactive.ProductRepository;
import com.maono.marketapplication.repositories.redis.RedisCartItemRepository;
import com.maono.marketapplication.repositories.redis.RedisProductRepository;
import com.maono.marketapplication.services.implementations.CartItemServiceImpl;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ExpectedCartItemTestDataProvider;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CartItemServiceImpl.class})
class CartItemServiceImplTest {
    @Autowired
    protected CartItemServiceImpl cartItemService;
    @MockitoBean
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected ProductRepository productRepository;
    @MockitoBean
    protected ProductActionStrategy productActionStrategy;
    @MockitoBean
    protected RedisCartItemRepository redisCartItemRepository;
    @MockitoBean
    protected RedisProductRepository redisProductRepository;

    @Test
    public void test_changeProductCountInTheCart() {
        when(productActionStrategy.execute(any(ProductActionType.class), anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.PLUS))
                        .verifyComplete();
        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.MINUS))
                        .verifyComplete();
        StepVerifier.create(cartItemService.changeProductCountInTheCart(1L, ProductActionType.DELETE))
                        .verifyComplete();

        verify(productActionStrategy, times(3)).execute(any(), anyLong());
        verifyNoMoreInteractions(productActionStrategy);
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    @Test
    public void test_findAllWithRelations_cartItemsNotExist() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());
        StepVerifier.create(cartItemService.findAllWithRelations()).expectNextCount(0).verifyComplete();

        verify(cartItemRepository).findAll();
        verifyNoMoreInteractions(cartItemRepository);
        verifyNoInteractions(productRepository, productActionStrategy, redisCartItemRepository, redisProductRepository);
    }

    @Test
    public void test_findAllWithRelations_allCached() {
        CartItem cartItemFromDb1 = cartItem(1L, 3).get();
        CartItem cartItemFromDb2 = cartItem(2L, 1).get();
        CartItem cartItemFromDb3 = cartItem(4L, 2).get();

        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                cartItemFromDb1,
                cartItemFromDb2,
                cartItemFromDb3));

        when(redisCartItemRepository.cacheObject(cartItemFromDb1)).thenReturn(Mono.just(cartItemFromDb1));
        when(redisCartItemRepository.cacheObject(cartItemFromDb2)).thenReturn(Mono.just(cartItemFromDb2));
        when(redisCartItemRepository.cacheObject(cartItemFromDb3)).thenReturn(Mono.just(cartItemFromDb3));

        Product cachedProduct1 = productByIdTemplate(1L).get();
        Product cachedProduct2 = productByIdTemplate(2L).get();
        Product cachedProduct4 = productByIdTemplate(4L).get();

        when(redisProductRepository.multiGet(List.of(1L, 2L, 4L)))
                .thenReturn(Flux.just(cachedProduct1, cachedProduct2, cachedProduct4));

        StepVerifier.create(cartItemService.findAllWithRelations())
                .assertNext(cartItem ->
                        assertEquals(cartItem(1L, 3).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(2L, 1).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(4L, 2).withProductByTemplate().get(), cartItem))
                .verifyComplete();

        verify(cartItemRepository).findAll();
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 1L) && item.getCount() == 3));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 2L) && item.getCount() == 1));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 4L) && item.getCount() == 2));
        verify(redisProductRepository).multiGet(List.of(1L, 2L, 4L));
        verifyNoMoreInteractions(cartItemRepository, redisCartItemRepository, redisProductRepository);
        verifyNoInteractions(productRepository, productActionStrategy);
    }

    @Test
    public void test_findAllWithRelations_ProductCachedPartly() {
        CartItem cartItemFromDb1 = cartItem(1L, 3).get();
        CartItem cartItemFromDb2 = cartItem(2L, 1).get();
        CartItem cartItemFromDb3 = cartItem(4L, 2).get();

        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                cartItemFromDb1,
                cartItemFromDb2,
                cartItemFromDb3));

        when(redisCartItemRepository.cacheObject(cartItemFromDb1)).thenReturn(Mono.just(cartItemFromDb1));
        when(redisCartItemRepository.cacheObject(cartItemFromDb2)).thenReturn(Mono.just(cartItemFromDb2));
        when(redisCartItemRepository.cacheObject(cartItemFromDb3)).thenReturn(Mono.just(cartItemFromDb3));

        Product cachedProduct1 = productByIdTemplate(1L).get();

        when(redisProductRepository.multiGet(List.of(1L, 2L, 4L)))
                .thenReturn(Flux.just(cachedProduct1));

        Product productFromDb2 = productByIdTemplate(2L).get();
        Product productFromDb3 = productByIdTemplate(4L).get();

        when(productRepository.findAllById(List.of(2L, 4L))).thenReturn(Flux.just(productFromDb2, productFromDb3));
        when(redisProductRepository.cacheObject(productFromDb2)).thenReturn(Mono.just(productFromDb2));
        when(redisProductRepository.cacheObject(productFromDb3)).thenReturn(Mono.just(productFromDb3));

        StepVerifier.create(cartItemService.findAllWithRelations())
                .assertNext(cartItem ->
                        assertEquals(cartItem(1L, 3).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(2L, 1).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(4L, 2).withProductByTemplate().get(), cartItem))
                .verifyComplete();

        verify(cartItemRepository).findAll();
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 1L) && item.getCount() == 3));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 2L) && item.getCount() == 1));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 4L) && item.getCount() == 2));
        verify(redisProductRepository).multiGet(List.of(1L, 2L, 4L));
        verify(productRepository).findAllById(List.of(2L, 4L));
        verify(redisProductRepository).cacheObject(argThat(product -> Objects.equals(2L, product.getId())));
        verify(redisProductRepository).cacheObject(argThat(product -> Objects.equals(4L, product.getId())));
        verifyNoMoreInteractions(cartItemRepository, redisCartItemRepository, redisProductRepository);
        verifyNoInteractions(productActionStrategy);
    }

    @Test
    public void test_findAllWithRelations_ProductNotCached() {
        CartItem cartItemFromDb1 = cartItem(1L, 3).get();
        CartItem cartItemFromDb2 = cartItem(2L, 1).get();
        CartItem cartItemFromDb3 = cartItem(4L, 2).get();

        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                cartItemFromDb1,
                cartItemFromDb2,
                cartItemFromDb3));

        when(redisCartItemRepository.cacheObject(cartItemFromDb1)).thenReturn(Mono.just(cartItemFromDb1));
        when(redisCartItemRepository.cacheObject(cartItemFromDb2)).thenReturn(Mono.just(cartItemFromDb2));
        when(redisCartItemRepository.cacheObject(cartItemFromDb3)).thenReturn(Mono.just(cartItemFromDb3));

        when(redisProductRepository.multiGet(List.of(1L, 2L, 4L)))
                .thenReturn(Flux.empty());

        Product productFromDb1 = productByIdTemplate(1L).get();
        Product productFromDb2 = productByIdTemplate(2L).get();
        Product productFromDb3 = productByIdTemplate(4L).get();

        when(productRepository.findAllById(List.of(1L, 2L, 4L)))
                .thenReturn(Flux.just(productFromDb1, productFromDb2, productFromDb3));
        when(redisProductRepository.cacheObject(productFromDb1)).thenReturn(Mono.just(productFromDb1));
        when(redisProductRepository.cacheObject(productFromDb2)).thenReturn(Mono.just(productFromDb2));
        when(redisProductRepository.cacheObject(productFromDb3)).thenReturn(Mono.just(productFromDb3));

        StepVerifier.create(cartItemService.findAllWithRelations())
                .assertNext(cartItem ->
                        assertEquals(cartItem(1L, 3).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(2L, 1).withProductByTemplate().get(), cartItem))
                .assertNext(cartItem ->
                        assertEquals(cartItem(4L, 2).withProductByTemplate().get(), cartItem))
                .verifyComplete();

        verify(cartItemRepository).findAll();
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 1L) && item.getCount() == 3));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 2L) && item.getCount() == 1));
        verify(redisCartItemRepository).cacheObject(argThat(item -> Objects.equals(item.getId(), 4L) && item.getCount() == 2));
        verify(redisProductRepository).multiGet(List.of(1L, 2L, 4L));
        verify(productRepository).findAllById(List.of(1L, 2L, 4L));
        verify(redisProductRepository).cacheObject(argThat(product -> Objects.equals(1L, product.getId())));
        verify(redisProductRepository).cacheObject(argThat(product -> Objects.equals(2L, product.getId())));
        verify(redisProductRepository).cacheObject(argThat(product -> Objects.equals(4L, product.getId())));
        verifyNoMoreInteractions(cartItemRepository, redisCartItemRepository, redisProductRepository);
        verifyNoInteractions(productActionStrategy);
    }

    @Test
    public void test_removeAll() {
        when(redisCartItemRepository.dropAllCounts()).thenReturn(Mono.empty());
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        StepVerifier.create(cartItemService.removeAll()).verifyComplete();

        verify(redisCartItemRepository).dropAllCounts();
        verify(cartItemRepository).deleteAll();
        verifyNoMoreInteractions(redisCartItemRepository, cartItemRepository);
        verifyNoInteractions(productRepository, productActionStrategy, redisProductRepository);
    }

    @Test
    public void test_calculateTotalSum() {
        List<CartItem> cartItemList = ExpectedCartItemTestDataProvider.cartItemList(List.of(1, 2, 3, 4));
        BigDecimal expected = BigDecimal.valueOf(101 + 102*2 + 103*3 + 104*4);

        assertEquals(expected, cartItemService.calculateTotalSum(cartItemList));
        verifyNoInteractions(cartItemRepository, cartItemRepository, productActionStrategy);
    }
}