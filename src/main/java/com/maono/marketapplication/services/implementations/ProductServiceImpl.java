package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.util.ProductSortType;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Page<Product> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber) {
        String sortBy = switch (sort) {
            case ALPHA -> "title";
            case PRICE -> "price";
            case NO -> "";
        };
        Pageable pageable = sortBy.isBlank() ?
                PageRequest.of(pageNumber, pageSize) :
                    PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
        if (search.isBlank()) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findProductByTitleContainingIgnoreCase(search, pageable);
        }
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

}
