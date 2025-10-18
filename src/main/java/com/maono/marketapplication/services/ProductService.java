package com.maono.marketapplication.services;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.util.ProductSortType;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Product> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber);
    Product findProductById(Long id);
}
