package com.maono.marketapplication.services;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.util.ProductSortType;
import jakarta.annotation.Nullable;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductService {
    Mono<Page<Product>> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber);
    Mono<Product> findProductByIdWithRelations(Long id);
    Mono<Void> importProducts(List<Product> productsToImport,
                              @Nullable Flux<FilePart> imagesToImport,
                              String imageDirPath);
}
