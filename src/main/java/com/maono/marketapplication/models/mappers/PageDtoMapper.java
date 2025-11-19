package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.responses.ProductsPageParametersDto;
import com.maono.marketapplication.repositories.util.Page;

public class PageDtoMapper {
    public static ProductsPageParametersDto mapToDto(Page<Product> page) {
        return new ProductsPageParametersDto(
                page.pageSize(),
                page.pageNumber(),
                page.hasNext(),
                page.hasPrevious());
    }
}
