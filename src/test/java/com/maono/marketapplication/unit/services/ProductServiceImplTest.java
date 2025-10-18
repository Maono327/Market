package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.implementations.ProductServiceImpl;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductById;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductsList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.getPage;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.getPageRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class, ProductRepository.class})
public class ProductServiceImplTest {
    @Autowired
    protected ProductServiceImpl productService;
    @MockitoBean
    protected ProductRepository productRepository;

    @MethodSource("arguments_test_findByPage")
    @ParameterizedTest
    public void test_findByPage(String search,
                                ProductSortType sortType,
                                int pageSize,
                                int pageNumber,
                                PageRequest pageRequest,
                                List<Product> products,
                                Page<Product> expected) {
        if (search.isBlank()) {
            when(productRepository.findAll(pageRequest)).thenReturn(
                    getPage(pageRequest, products));
            assertEquals(expected, productService.findByPage(search, sortType, pageSize, pageNumber));
            verify(productRepository).findAll(pageRequest);
            verify(productRepository, never()).findProductByTitleContainingIgnoreCase(anyString(), any());
            verify(productRepository, never()).findById(anyLong());
        } else {
            when(productRepository.findProductByTitleContainingIgnoreCase(search, pageRequest)).thenReturn(
                    getPage(pageRequest, products));
            assertEquals(expected, productService.findByPage(search, sortType, pageSize, pageNumber));
            verify(productRepository).findProductByTitleContainingIgnoreCase(search, pageRequest);
            verifyNoMoreInteractions(productRepository);
        }
    }

    @Test
    public void test_findProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.ofNullable(buildProductById(1L)));
        Product expected = buildProductById(1L);
        Product result = productService.findProductById(1L);
        assertEquals(expected, result);
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void test_findProductById_throw_NoSuchElementException() {
        when(productRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> productService.findProductById(6L));
        verify(productRepository).findById(6L);
        verifyNoMoreInteractions(productRepository);
    }

    protected static Stream<Arguments> arguments_test_findByPage() {
        return Stream.of(
            getArguments("",
                    ProductSortType.NO, 5, 1, buildProductsList(1L, 2L, 3L, 4L, 5L)),
            getArguments("",
                    ProductSortType.ALPHA, 5, 1, buildProductsList(3L, 5L, 4L, 1L, 2L)),
            getArguments("",
                    ProductSortType.PRICE, 5, 1, buildProductsList(1L, 4L, 2L, 3L, 5L)),
            getArguments("кн",
                    ProductSortType.NO, 5, 1, buildProductsList(1L)),
            getArguments("а",
                    ProductSortType.ALPHA, 5, 1, buildProductsList(5L, 1L)),
            getArguments("а",
                    ProductSortType.PRICE, 5, 1, buildProductsList(1L, 5L)),
            getArguments("",
                        ProductSortType.NO, 2, 1, buildProductsList(1L, 2L)),
            getArguments("",
                        ProductSortType.NO, 2, 2, buildProductsList(3L, 4L)),
            getArguments("",
                        ProductSortType.NO, 2, 3, buildProductsList(5L))
        );
    }

    protected static Arguments getArguments(String search,
                                            ProductSortType sortType,
                                            int pageSize,
                                            int pageNumber,
                                            List<Product> expectedProducts) {
        PageRequest pageRequest = getPageRequest(pageSize, pageNumber, sortType);
        return Arguments.of(
                search,
                sortType,
                pageSize,
                pageNumber,
                pageRequest,
                expectedProducts,
                getPage(pageRequest, expectedProducts));
    }
}
