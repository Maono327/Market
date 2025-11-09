package com.maono.marketapplication.util;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.buildCartItemByProduct;

public class ExpectedProductsTestDataProvider {
    public static List<Product> buildProductsList(Long... ids) {
        return Arrays.stream(ids).map(ExpectedProductsTestDataProvider::buildProductById).toList();
    }

    public static List<Product> buildProductsList(Pair<Long, Integer>... products) {
        return Arrays.stream(products)
                .map(pair -> {
                    Product p = buildProductById(pair.getFirst());
                    p.setCartItem(buildCartItemByProduct(p, pair.getSecond()));
                    return p;
                }).toList();
    }

    public static Product buildProductById(long id) {
        if (id == 1L) return buildBookProduct();
        if (id == 2L) return buildBriefcaseProduct();
        if (id == 3L) return buildPolaroidProduct();
        if (id == 4L) return buildUmbrellaProduct();
        if (id == 5L) return buildVaseProduct();
        if (id == -1L) return stubProduct();
        return null;
    }

    public static Product buildProductByIdWithCartItem(Long id, int count) {
        Product product = buildProductById(id);
        buildCartItemByProduct(product, count);
        return product;
    }

    public static Product stubProduct() {
        Product product = Product.builder()
                .id(-1L)
                .build();
        CartItem cartItem = CartItem.builder().count(0).build();
        product.setCartItem(cartItem);
        cartItem.setProduct(product);
        return product;
    }

    public static Product buildBookProduct() {
        return Product.builder()
                .id(1L)
                .title("Книга")
                .description("Интересная книга")
                .imageName("book.png")
                .price(BigDecimal.valueOf(699.99))
                .build();
    }

    public static Product buildBriefcaseProduct() {
        return Product.builder()
                .id(2L)
                .title("Портфель")
                .description("Удобный и красивый портфель")
                .imageName("briefcase.png")
                .price(new BigDecimal("4000.00"))
                .build();
    }

    public static Product buildPolaroidProduct() {
        return Product.builder()
                .id(3L)
                .title("Polaroid")
                .description("Для крутых фотографий")
                .imageName("polaroid.png")
                .price(BigDecimal.valueOf(6599.99))
                .build();
    }

    public static Product buildUmbrellaProduct() {
        return Product.builder()
                .id(4L)
                .title("Зонтик")
                .description("Красивый и прозрачный зонт")
                .imageName("umbrella.png")
                .price(BigDecimal.valueOf(1999.99))
                .build();
    }

    public static Product buildVaseProduct() {
        return Product.builder()
                .id(5L)
                .title("Ваза")
                .description("Дизайнерская ваза")
                .imageName("vase.png")
                .price(BigDecimal.valueOf(12999.99))
                .build();
    }

    public static List<ProductDto> buildProductDtoList(List<Product> products) {
        return products.stream().map(ExpectedProductsTestDataProvider::mapToProductDto).toList();
    }

    public static List<ProductDto> buildProductDtoList(Pair<Long, Integer>... products) {
        return Arrays.stream(products)
                .map(product -> mapToProductDto(
                        buildProductById(product.getFirst()), product.getSecond()))
                .toList();
    }

    public static List<ProductDto> buildProductDtoList(Long... ids) {
        return Arrays.stream(ids).map(id -> mapToProductDto(buildProductByIdWithCartItem(id, 0))).toList();
    }

    public static ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .count(product.getCartItem().getCount())
                .imgPath(product.getImageName())
                .build();
    }

    public static ProductDto mapToProductDto(Product product, int count) {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .count(count)
                .imgPath(product.getImageName())
                .build();
    }


    public static PageRequest getPageRequest(int pageSize, int pageNumber, ProductSortType sortType) {
        if (sortType == ProductSortType.NO) {
            return PageRequest.of(pageNumber, pageSize, Sort.unsorted());
        } else {
            Sort sort = switch (sortType) {
                case ALPHA -> Sort.by("title").ascending();
                case PRICE -> Sort.by("price").ascending();
                default -> throw new IllegalStateException(String.valueOf(sortType));
            };
            return PageRequest.of(pageNumber, pageSize, sort);
        }
    }

    public static Page<Product> getPage(PageRequest pageRequest,
                                           List<Product> expectedProducts) {
        return new PageImpl<>(
                expectedProducts,
                pageRequest,
                expectedProducts.size());
    }
}
