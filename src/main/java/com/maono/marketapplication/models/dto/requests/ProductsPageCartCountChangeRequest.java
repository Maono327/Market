package com.maono.marketapplication.models.dto.requests;

import com.maono.marketapplication.util.ProductActionType;
import com.maono.marketapplication.util.ProductSortType;

public record ProductsPageCartCountChangeRequest(Long id,
                                                 String search,
                                                 ProductSortType sort,
                                                 Integer pageSize,
                                                 Integer pageNumber,
                                                 ProductActionType action) {
    public ProductsPageCartCountChangeRequest {
        if (search == null) search = "";
        if (sort == null) sort = ProductSortType.NO;
        if (pageSize == null) pageSize = 5;
        if (pageNumber == null) pageNumber = 1;
    }
}
