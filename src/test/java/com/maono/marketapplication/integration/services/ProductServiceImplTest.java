package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductById;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductByIdWithCartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.getPage;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(PostgresqlContainerConfiguration.class)
public class ProductServiceImplTest {
    @Autowired
    protected ProductService productService;

    @Test
    public void test_findByPage() {
        Page<Product> expected = getPage(PageRequest.of(0,5, Sort.unsorted()), List.of(
                buildProductByIdWithCartItem(1L, 3),
                buildProductByIdWithCartItem(2L, 1),
                buildProductById(3L),
                buildProductById(4L),
                buildProductByIdWithCartItem(5L, 2)
        ));

        assertEquals(expected, productService.findByPage("",
                ProductSortType.NO, 5, 0));
    }

    @Test
    public void test_findByPage_search() {
        Page<Product> expected = getPage(PageRequest.of(0,5, Sort.unsorted()), List.of(
                buildProductByIdWithCartItem(2L, 1),
                buildProductById(4L)
        ));

        assertEquals(expected, productService.findByPage("о",
                ProductSortType.NO, 5, 0));
    }

    @Test
    public void test_findByPage_sort_alpha() {
        Page<Product> expected = getPage(PageRequest.of(0,5,
                                                                    Sort.by("title").ascending()), List.of(
                buildProductById(3L),
                buildProductByIdWithCartItem(5L, 2),
                buildProductById(4L),
                buildProductByIdWithCartItem(1L, 3),
                buildProductByIdWithCartItem(2L, 1)
        ));

        assertEquals(expected, productService.findByPage("",
                ProductSortType.ALPHA, 5, 0));
    }

    @Test
    public void test_findByPage_sort_price() {
        Page<Product> expected = getPage(PageRequest.of(0,5,
                Sort.by("price").ascending()), List.of(
                buildProductByIdWithCartItem(1L, 3),
                buildProductById(4L),
                buildProductByIdWithCartItem(2L, 1),
                buildProductById(3L),
                buildProductByIdWithCartItem(5L, 2)
        ));

        assertEquals(expected, productService.findByPage("",
                ProductSortType.PRICE, 5, 0));
    }

    @Test
    public void test_findByPage_mixed() {
        Page<Product> expected = getPage(PageRequest.of(0,2, Sort.by("price").ascending()),
            List.of(
                    buildProductById(4L),
                    buildProductByIdWithCartItem(2L, 1)
            ));

        assertEquals(expected, productService.findByPage("о",
                ProductSortType.PRICE, 2, 0));
    }

    @Test
    public void test_findProductById() {
        assertEquals(buildProductByIdWithCartItem(1L, 3), productService.findProductById(1L));
    }
}
