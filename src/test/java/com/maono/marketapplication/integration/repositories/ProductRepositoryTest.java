package com.maono.marketapplication.integration.repositories;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.ProductRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductById;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductsList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(PostgresqlContainerConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    protected ProductRepository productRepository;

    @ParameterizedTest
    @MethodSource("arguments_test_findProductByTitleContainingIgnoreCase")
    public void test_findProductByTitleContainingIgnoreCase(String title, Pageable pageable, Page<Product> expected) {
        Page<Product> result = productRepository.findProductByTitleContainingIgnoreCase(title, pageable);
        assertEquals(expected, result);
    }

    protected static Stream<Arguments> arguments_test_findProductByTitleContainingIgnoreCase() {
        return Stream.of(
                getArguments("", 1, 5, buildProductsList(1L, 2L, 3L, 4L, 5L)),
                getArguments("о", 1, 5, buildProductsList(2L, 4L)),
                getArguments("", 1, 5, buildProductsList(3L, 5L, 4L, 1L, 2L),
                        Sort.by("title").ascending()),
                getArguments("", 1, 5, buildProductsList(1L, 4L, 2L, 3L, 5L),
                        Sort.by("price").ascending()),
                getArguments("о", 1, 2, buildProductsList(4L, 2L),
                        Sort.by("price").ascending()),
                getArguments("", 3, 2, buildProductsList(5L)),
                getArguments("laroi", 1, 2, buildProductsList(3L)),
                getArguments("кни", 1, 3, buildProductsList(1L))
        );
    }

    protected static Arguments getArguments(String title,
                                            int pageNumber,
                                            int pageSize,
                                            List<Product> expectedProducts) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Product> expectedPage= new PageImpl<>(expectedProducts, pageable, expectedProducts.size());
        return Arguments.of(title, pageable, expectedPage);
    }

    protected static Arguments getArguments(String title,
                                            int pageNumber,
                                            int pageSize,
                                            List<Product> expectedProducts,
                                            Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Page<Product> expectedPage= new PageImpl<>(expectedProducts, pageable, expectedProducts.size());
        return Arguments.of(title, pageable, expectedPage);
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L})
    @ParameterizedTest
    public void test_findById(Long id) {
        Optional<Product> expected = Optional.ofNullable(buildProductById(id));
        assertEquals(expected, productRepository.findById(id));
    }
}