package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.responses.ProductDto;

public class ProductDtoMapper {
    public static ProductDto mapProductToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getImageName(),
                product.getPrice(),
                product.getCartItem() == null ? 0 : product.getCartItem().getCount());
    }
}
