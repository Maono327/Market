package com.maono.marketapplication.services;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.util.ProductSortType;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Page<Product>> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber);
    Mono<Product> findProductByIdWithRelations(Long id);
}
