package com.maono.marketapplication.repositories;

import com.maono.marketapplication.models.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {
    @Query("""
            SELECT p.id, 
                   p.title, 
                   p.description, 
                   p.image_name,
                   p.price
                   FROM products p
                   ORDER BY
                       CASE WHEN :sortBy = 'title' THEN p.title END,
                       CASE WHEN :sortBy = 'price' THEN p.price END,
                       p.id
                   LIMIT :pageSize OFFSET :offset
           """)
    Flux<Product> findProductsByPage(int pageSize, int offset, String sortBy);

    @Query("SELECT count(*) FROM products")
    Mono<Integer> totalCount();

    @Query("""
            SELECT p.id, 
                   p.title, 
                   p.description, 
                   p.image_name,
                   p.price
                   FROM products p
                   WHERE title ILIKE '%' || :search || '%'
                   ORDER BY
                       CASE WHEN :sortBy = 'title' THEN p.title END,
                       CASE WHEN :sortBy = 'price' THEN p.price END,
                       p.id
                   LIMIT :pageSize OFFSET :offset
           """)
    Flux<Product> findProductsByPageAndTitle(String search, int pageSize, int offset, String sortBy);

    Mono<Product> findProductByTitleLike(String title);
}
