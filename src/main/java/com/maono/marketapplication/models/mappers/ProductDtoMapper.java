package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.responses.ProductDto;

public class ProductDtoMapper {
    public static ProductDto mapProductToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .imgPath(product.getImageName())
                .price(product.getPrice())
                .count(product.getCartItem() == null ? 0 : product.getCartItem().getCount())
                .build();
    }
}
