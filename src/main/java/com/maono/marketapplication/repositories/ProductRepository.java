package com.maono.marketapplication.repositories;

import com.maono.marketapplication.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findProductByTitleContainingIgnoreCase(String title, Pageable page);

    Product findProductByTitle(String title);
}
