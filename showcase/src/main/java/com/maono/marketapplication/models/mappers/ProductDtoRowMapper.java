package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.responses.ProductDto;

import java.util.ArrayList;
import java.util.List;

public class ProductDtoRowMapper {
    public static List<List<ProductDto>> mapProductDtoRow(List<Product> productsWithCartItems) {
        List<List<ProductDto>> items = new ArrayList<>();
        List<ProductDto> row = new ArrayList<>();
        for (int i = 0;
             i < (productsWithCartItems.size() + (productsWithCartItems.size() % 3 == 0 ?
                     0 : 3 - (productsWithCartItems.size() % 3)));
             i++) {
            if (i < productsWithCartItems.size()) {
                Product product = productsWithCartItems.get(i);
                row.add(ProductDtoMapper.mapProductToDto(product));
            } else {
                row.add(ProductDto.builder().id(-1).build());
            }
            if (row.size() == 3) {
                items.add(row);
                row = new ArrayList<>();
            }
        }
        return items;
    }
}
