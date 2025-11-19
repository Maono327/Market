package com.maono.marketapplication.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.util.Page;

import java.math.BigDecimal;
import java.util.List;

public class ExpectedProductsTestDataProvider {

    public static class StagedProductTestDataBuilder {
        private final Product product;

        public StagedProductTestDataBuilder(Product product) {
            this.product = product;
        }

        public StagedProductTestDataBuilder withCartItemByCount(int count) {
            CartItem c = CartItem.builder()
                    .id(product.getId())
                    .count(count)
                    .product(product)
                    .build();
            product.setCartItem(c);
            return this;
        }

        public StagedProductTestDataBuilder withPrice(double price) {
            product.setPrice(BigDecimal.valueOf(price));
            return this;
        }

        public StagedProductTestDataBuilder withTitle(String title) {
            product.setTitle(title);
            return this;
        }

        public Product get() {
            return product;
        }
    }

    public static StagedProductTestDataBuilder productByIdTemplate(Long id) {
        String productTemplate = "Product" + id;
        Product p = Product.builder()
                            .id(id)
                            .title("Title" + productTemplate)
                            .description("Description" + productTemplate)
                            .price(BigDecimal.valueOf(100 + id))
                            .imageName("image" + productTemplate + ".png")
                            .build();
        return new StagedProductTestDataBuilder(p);
    }

    public static Product stubProduct() {
        return Product.builder().id(-1L).build();
    }

    public static Page<Product> page(List<Product> items,
                              int pageSize,
                              int pageNumber,
                              boolean hasNext,
                              boolean hasPrevious,
                              int totalPages) {
        return new Page<>(items,pageSize,pageNumber,hasNext,hasPrevious,totalPages);
    }

    public static StagedProductTestDataBuilder bookProduct() {
        return new StagedProductTestDataBuilder(Product.builder()
                .id(1L)
                .title("Книга")
                .description("Интересная книга")
                .imageName("book.png")
                .price(BigDecimal.valueOf(699.99))
                .build());
    }

    public static StagedProductTestDataBuilder briefcaseProduct() {
        return new StagedProductTestDataBuilder(Product.builder()
                .id(2L)
                .title("Портфель")
                .description("Удобный и красивый портфель")
                .imageName("briefcase.png")
                .price(new BigDecimal("4000.00"))
                .build());
    }

    public static StagedProductTestDataBuilder polaroidProduct() {
        return new StagedProductTestDataBuilder(Product.builder()
                .id(3L)
                .title("Polaroid")
                .description("Для крутых фотографий")
                .imageName("polaroid.png")
                .price(BigDecimal.valueOf(6599.99))
                .build());
    }

    public static StagedProductTestDataBuilder umbrellaProduct() {
        return new StagedProductTestDataBuilder(Product.builder()
                .id(4L)
                .title("Зонтик")
                .description("Красивый и прозрачный зонт")
                .imageName("umbrella.png")
                .price(BigDecimal.valueOf(1999.99))
                .build());
    }

    public static StagedProductTestDataBuilder vaseProduct() {
        return new StagedProductTestDataBuilder(Product.builder()
                .id(5L)
                .title("Ваза")
                .description("Дизайнерская ваза")
                .imageName("vase.png")
                .price(BigDecimal.valueOf(12999.99))
                .build());
    }
}
