package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.ImportProduct;

public class ImportProductMapper {
    public static Product map(ImportProduct importProduct) {
        return Product.builder()
                .title(importProduct.title())
                .description(importProduct.description())
                .imageName(importProduct.imageName())
                .price(importProduct.price())
                .build();
    }
}
