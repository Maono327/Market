package com.maono.marketapplication.integration.repositories;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.bookProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.briefcaseProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.polaroidProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.umbrellaProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.vaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DataR2dbcTest
@Import(IntegrationTestConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    protected ProductRepository productRepository;

    @ParameterizedTest
    @MethodSource("arguments_test_findProductsByPage")
    public void test_findProductsByPage(int pageSize,
                                        int offset,
                                        String sortBy,
                                        List<Product> expected) {
        StepVerifier.Step<Product> step =
                StepVerifier.create(productRepository.findProductsByPage(pageSize, offset, sortBy));

        for (Product ep : expected) {
            step = step.assertNext(p -> {
                assertEquals(ep.getId(), p.getId());
                assertEquals(ep.getTitle(), p.getTitle());
                assertEquals(ep.getPrice(), p.getPrice());
                assertEquals(ep.getDescription(), p.getDescription());
                assertEquals(ep.getImageName(), p.getImageName());
            });
        }
        step.verifyComplete();
    }

    protected static Stream<Arguments> arguments_test_findProductsByPage() {
        return Stream.of(
                Arguments.arguments(5, 0, "",
                        List.of(bookProduct().get(),
                                briefcaseProduct().get(),
                                polaroidProduct().get(),
                                umbrellaProduct().get(),
                                vaseProduct().get())),
                Arguments.arguments(5, 0, "title",
                        List.of(polaroidProduct().get(),
                                vaseProduct().get(),
                                umbrellaProduct().get(),
                                bookProduct().get(),
                                briefcaseProduct().get())),
                Arguments.arguments(5, 0, "price",
                        List.of(bookProduct().get(),
                                umbrellaProduct().get(),
                                briefcaseProduct().get(),
                                polaroidProduct().get(),
                                vaseProduct().get())),
                Arguments.arguments(2, 4, "", List.of(vaseProduct().get())),
                Arguments.arguments(2, 2, "", List.of(polaroidProduct().get(), umbrellaProduct().get())),
                Arguments.arguments(2, 2, "title", List.of(umbrellaProduct().get(), bookProduct().get())),
                Arguments.arguments(2, 2, "price", List.of(briefcaseProduct().get(), polaroidProduct().get()))
        );
    }

    @ParameterizedTest
    @MethodSource("arguments_test_findProductByPageAndTitle")
    public void test_findProductsByPageAndTitle(String title,
                                                int pageSize,
                                                int offset,
                                                String sortBy,
                                                List<Product> expected) {
        StepVerifier.Step<Product> step =
                StepVerifier.create(productRepository.findProductsByPageAndTitle(title, pageSize, offset, sortBy));

        for (Product ep : expected) {
            step = step.assertNext(p -> {
                assertEquals(ep.getId(), p.getId());
                assertEquals(ep.getTitle(), p.getTitle());
                assertEquals(ep.getPrice(), p.getPrice());
                assertEquals(ep.getDescription(), p.getDescription());
                assertEquals(ep.getImageName(), p.getImageName());
            });
        }
        step.verifyComplete();
    }

    protected static Stream<Arguments> arguments_test_findProductByPageAndTitle() {
        return Stream.of(
                Arguments.arguments("о", 5, 0, "", List.of(briefcaseProduct().get(), umbrellaProduct().get())),
                Arguments.arguments("PO", 5, 0, "", List.of(polaroidProduct().get())),
                Arguments.arguments("о", 2, 0, "price", List.of(umbrellaProduct().get(), briefcaseProduct().get()))
                );
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L})
    @ParameterizedTest
    public void test_findById(Long id) {
        if (id <= 5L) {
            StepVerifier.create(productRepository.findById(id))
                    .assertNext(product -> {
                        Product expected = expectedProductById(id);
                        assertEquals(expected.getId(), product.getId());
                        assertEquals(expected.getTitle(), product.getTitle());
                        assertEquals(expected.getDescription(), product.getDescription());
                        assertEquals(expected.getImageName(), product.getImageName());
                        assertEquals(expected.getPrice(), product.getPrice());
                    })
                    .verifyComplete();
        } else {
            StepVerifier.create(productRepository.findById(id))
                    .expectNextCount(0)
                    .verifyComplete();
        }
    }

    @Test
    public void test_findAllById() {
        List<Long> ids = List.of(1L, 3L, 5L);

        StepVerifier.create(productRepository.findAllById(ids))
                .assertNext(product -> {
                    Product expected = bookProduct().get();

                    assertEquals(expected.getId(), product.getId());
                    assertEquals(expected.getTitle(), product.getTitle());
                    assertEquals(expected.getDescription(), product.getDescription());
                    assertEquals(expected.getPrice(), product.getPrice());
                    assertEquals(expected.getImageName(), product.getImageName());
                })
                .assertNext(product -> {
                    Product expected = polaroidProduct().get();

                    assertEquals(expected.getId(), product.getId());
                    assertEquals(expected.getTitle(), product.getTitle());
                    assertEquals(expected.getDescription(), product.getDescription());
                    assertEquals(expected.getPrice(), product.getPrice());
                    assertEquals(expected.getImageName(), product.getImageName());
                })
                .assertNext(product -> {
                    Product expected = vaseProduct().get();

                    assertEquals(expected.getId(), product.getId());
                    assertEquals(expected.getTitle(), product.getTitle());
                    assertEquals(expected.getDescription(), product.getDescription());
                    assertEquals(expected.getPrice(), product.getPrice());
                    assertEquals(expected.getImageName(), product.getImageName());
                })
                .verifyComplete();
    }

    @Test
    public void test_totalCount() {
        StepVerifier.create(productRepository.totalCount())
                .assertNext(count -> assertEquals(5, count))
                .verifyComplete();
    }

    protected static Product expectedProductById(Long id) {
        if (id == 1L) return bookProduct().get();
        if (id == 2L) return briefcaseProduct().get();
        if (id == 3L) return polaroidProduct().get();
        if (id == 4L) return umbrellaProduct().get();
        if (id == 5L) return vaseProduct().get();
        return productByIdTemplate(id).get();
    }
}