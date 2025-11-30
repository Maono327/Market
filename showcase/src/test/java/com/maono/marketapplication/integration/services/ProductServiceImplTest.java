package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.bookProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.briefcaseProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.page;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.polaroidProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.umbrellaProduct;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.vaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
public class ProductServiceImplTest {
    @Autowired
    protected ProductService productService;

    @Test
    public void test_findByPage() {
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
                    List<CartItem> expectedCartItems =  expected.items().stream().map(Product::getCartItem).toList();
                    List<CartItem> pageCartItems = productPage.items().stream().map(Product::getCartItem).toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();
    }

    @Test
    public void test_findByPage_search() {
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
                    List<CartItem> expectedCartItems =  expected.items().stream().map(Product::getCartItem).toList();
                    List<CartItem> pageCartItems = productPage.items().stream().map(Product::getCartItem).toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();
    }

    @Test
    public void test_findByPage_sort_alpha() {
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
                    List<CartItem> expectedCartItems =  expected.items().stream().map(Product::getCartItem).toList();
                    List<CartItem> pageCartItems = productPage.items().stream().map(Product::getCartItem).toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();
    }

    @Test
    public void test_findByPage_sort_price() {
        Page<Product> expected = page(
                List.of(bookProduct().withCartItemByCount(3).get(),
                        umbrellaProduct().get(),
                        briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get(),
                        vaseProduct().withCartItemByCount(2).get()),
                5,
                1,
                false,
                false,
                1
        );

        StepVerifier.create(productService.findByPage("", ProductSortType.PRICE, 5, 1))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream().map(Product::getCartItem).toList();
                    List<CartItem> pageCartItems = productPage.items().stream().map(Product::getCartItem).toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertFalse(productPage.hasNext());
                    assertFalse(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();
    }

    @Test
    public void test_findByPage_mixed() {
        Page<Product> expected = page(
                List.of(briefcaseProduct().withCartItemByCount(1).get(),
                        polaroidProduct().get()),
                2,
                2,
                true,
                true,
                3
        );

        StepVerifier.create(productService.findByPage("", ProductSortType.PRICE, 2, 2))
                .assertNext(productPage -> {
                    List<CartItem> expectedCartItems =  expected.items().stream().map(Product::getCartItem).toList();
                    List<CartItem> pageCartItems = productPage.items().stream().map(Product::getCartItem).toList();

                    assertEquals(expectedCartItems, pageCartItems);
                    assertEquals(expected.items(), productPage.items());
                    assertEquals(expected.pageSize(), productPage.pageSize());
                    assertEquals(expected.pageNumber(), productPage.pageNumber());
                    assertTrue(productPage.hasNext());
                    assertTrue(productPage.hasPrevious());
                    assertEquals(expected.totalPages(), productPage.totalPages());
                })
                .verifyComplete();
    }

    @Test
    public void test_findProductByIdWithRelations() {
        StepVerifier.create(productService.findProductByIdWithRelations(1L))
                .assertNext(product -> {
                    Product expected = bookProduct().withCartItemByCount(3).get();
                    assertEquals(expected, product);
                    assertEquals(expected.getCartItem(), product.getCartItem());
                })
                .verifyComplete();
    }
}
