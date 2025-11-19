package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.services.implementations.ProductServiceImpl;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.page;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class})
public class ProductServiceImplTest {
    @Autowired
    protected ProductServiceImpl productService;
    @MockitoBean
    protected ProductRepository productRepository;
    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MethodSource("arguments_test_findByPage")
    @ParameterizedTest
    public void test_findByPage(String search,
                                ProductSortType sortType,
                                int pageSize,
                                int pageNumber,
                                int totalCount,
                                List<Product> productsFromDb,
                                List<CartItem> cartItemsFromDb,
                                Page<Product> expectedPage) {
        if (search.isBlank()) {
            when(productRepository.findProductsByPage(anyInt(), anyInt(), anyString()))
                    .thenReturn(Flux.fromIterable(productsFromDb));
        } else {
            when(productRepository.findProductsByPageAndTitle(anyString(), anyInt(),
                    anyInt(), anyString()))
                    .thenReturn(Flux.fromIterable(productsFromDb));
        }
        when(cartItemRepository.findAllById(anyList())).thenReturn(Flux.fromIterable(cartItemsFromDb));
        when(productRepository.totalCount()).thenReturn(Mono.just(totalCount));

        StepVerifier.create(productService.findByPage(search, sortType, pageSize, pageNumber))
                .assertNext(page -> {
                    assertEquals(expectedPage.items(), page.items());
                    assertEquals(expectedPage.pageSize(), page.pageSize());
                    assertEquals(expectedPage.pageNumber(), page.pageNumber());
                    assertEquals(expectedPage.totalPages(), page.totalPages());

                })
                .verifyComplete();

        verify(productRepository).totalCount();
        if (search.isBlank()) {
            verify(productRepository).findProductsByPage(anyInt(), anyInt(), anyString());
        } else {
            verify(productRepository).findProductsByPageAndTitle(anyString(), anyInt(), anyInt(), anyString());
        }
        verify(cartItemRepository).findAllById(anyList());
        verifyNoMoreInteractions(productRepository, cartItemRepository);
    }

    @Test
    public void test_findProductById_WithRelations_existCartItem() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(productByIdTemplate(1L).get()));
        when(cartItemRepository.findById(anyLong())).thenReturn(Mono.just(cartItem(1L, 2).get()));

        Product expected = productByIdTemplate(1L).withCartItemByCount(2).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> assertEquals(expected, product))
                .verifyComplete();

        verify(productRepository).findById(anyLong());
        verify(cartItemRepository).findById(anyLong());
        verifyNoMoreInteractions(productRepository, cartItemRepository);
    }

    @Test
    public void test_findProductById_WithRelations_withoutCartItem() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(productByIdTemplate(1L).get()));
        when(cartItemRepository.findById(anyLong())).thenReturn(Mono.empty());

        Product expected = productByIdTemplate(1L).get();

        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    assertEquals(expected, product);
                })
                .verifyComplete();

        verify(productRepository).findById(anyLong());
        verify(cartItemRepository).findById(anyLong());
        verifyNoMoreInteractions(productRepository, cartItemRepository);
    }

    protected static Stream<Arguments> arguments_test_findByPage() {
        return Stream.of(
                getArguments("",
                        ProductSortType.NO,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(1L).get(),
                                productByIdTemplate(2L).get(),
                                productByIdTemplate(3L).get(),
                                productByIdTemplate(4L).get(),
                                productByIdTemplate(5L).get()
                        ),
                        List.of(
                                cartItem(1L, 1).get(),
                                cartItem(3L, 2).get(),
                                cartItem(5L, 5).get()
                        ),
                        List.of(
                                productByIdTemplate(1L).withCartItemByCount(1).get(),
                                productByIdTemplate(2L).get(),
                                productByIdTemplate(3L).withCartItemByCount(2).get(),
                                productByIdTemplate(4L).get(),
                                productByIdTemplate(5L).withCartItemByCount(5).get())),
                getArguments("",
                        ProductSortType.ALPHA,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(3L).withTitle("A").get(),
                                productByIdTemplate(5L).withTitle("B").get(),
                                productByIdTemplate(4L).withTitle("C").get(),
                                productByIdTemplate(1L).withTitle("D").get(),
                                productByIdTemplate(2L).withTitle("E").get()
                        ),
                        List.of(
                                cartItem(3L, 2).get(),
                                cartItem(4L, 2).get()
                        ),
                        List.of(
                                productByIdTemplate(3L).withTitle("A").withCartItemByCount(2).get(),
                                productByIdTemplate(5L).withTitle("B").get(),
                                productByIdTemplate(4L).withTitle("C").withCartItemByCount(2).get(),
                                productByIdTemplate(1L).withTitle("D").get(),
                                productByIdTemplate(2L).withTitle("E").get()
                        )),
                getArguments("",
                        ProductSortType.PRICE,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(1L).withPrice(10).get(),
                                productByIdTemplate(4L).withPrice(20).get(),
                                productByIdTemplate(2L).withPrice(30).get(),
                                productByIdTemplate(3L).withPrice(40).get(),
                                productByIdTemplate(5L).withPrice(50).get()
                        ),
                        List.of(
                                cartItem(1L, 1).get(),
                                cartItem(3L, 2).get(),
                                cartItem(5L, 5).get()
                        ),
                        List.of(
                                productByIdTemplate(1L).withPrice(10).withCartItemByCount(1).get(),
                                productByIdTemplate(4L).withPrice(20).get(),
                                productByIdTemplate(2L).withPrice(30).get(),
                                productByIdTemplate(3L).withPrice(40).withCartItemByCount(2).get(),
                                productByIdTemplate(5L).withPrice(50).withCartItemByCount(5).get()
                        )),
                getArguments("Bo",
                        ProductSortType.NO,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(1L).withTitle("Book").get()
                        ),
                        List.of(
                                cartItem(1L, 1).get()
                        ),
                        List.of(
                                productByIdTemplate(1L).withTitle("Book").withCartItemByCount(1).get()
                        )),
                getArguments("Bo",
                        ProductSortType.ALPHA,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(2L).withTitle("Bob").get(),
                                productByIdTemplate(1L).withTitle("Book").get()
                        ),
                        List.of(
                                cartItem(1L, 1).get()
                        ),
                        List.of(
                                productByIdTemplate(2L).withTitle("Bob").get(),
                                productByIdTemplate(1L).withTitle("Book").withCartItemByCount(1).get()
                        )),
                getArguments("Bo",
                        ProductSortType.PRICE,
                        5,
                        1,
                        false,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(1L).withPrice(10).get(),
                                productByIdTemplate(2L).withPrice(20).get()
                        ),
                        List.of(
                                cartItem(2L, 2).get()
                        ),
                        List.of(
                                productByIdTemplate(1L).withPrice(10).get(),
                                productByIdTemplate(2L).withPrice(20).withCartItemByCount(2).get()
                        )),
                getArguments("",
                        ProductSortType.NO,
                        2,
                        1,
                        true,
                        false,
                        5,
                        List.of(
                                productByIdTemplate(1L).get(),
                                productByIdTemplate(2L).get()
                        ),
                        List.of(
                                cartItem(1L, 1).get(),
                                cartItem(2L, 5).get()
                        ),
                        List.of(
                                productByIdTemplate(1L).withCartItemByCount(1).get(),
                                productByIdTemplate(2L).withCartItemByCount(5).get()
                        )),
                getArguments("",
                        ProductSortType.NO,
                        2,
                        2,
                        true,
                        true,
                        5,
                        List.of(
                                productByIdTemplate(3L).get(),
                                productByIdTemplate(4L).get()
                        ),
                        List.of(
                                cartItem(3L, 2).get()
                        ),
                        List.of(
                                productByIdTemplate(3L).withCartItemByCount(2).get(),
                                productByIdTemplate(4L).get()
                        )),
                getArguments("",
                        ProductSortType.NO,
                        2,
                        3,
                        false,
                        true,
                        5,
                        List.of(
                                productByIdTemplate(5L).get()
                        ),
                        List.of(
                                cartItem(5L, 5).get()
                        ),
                        List.of(productByIdTemplate(5L).withCartItemByCount(5).get()))
        );
    }

    protected static Arguments getArguments(String search,
                                            ProductSortType sortType,
                                            int pageSize,
                                            int pageNumber,
                                            boolean hasNext,
                                            boolean hasPrevious,
                                            int totalCount,
                                            List<Product> productsFromDb,
                                            List<CartItem> cartItemsFromDb,
                                            List<Product> expectedPageProducts) {
        Page<Product> expectedPage = page(
                expectedPageProducts,
                pageSize,
                pageNumber,
                hasNext,
                hasPrevious,
                (int) Math.ceil((double) totalCount / pageSize));

        return Arguments.of(
                search,
                sortType,
                pageSize,
                pageNumber,
                totalCount,
                productsFromDb,
                cartItemsFromDb,
                expectedPage);
    }
}
