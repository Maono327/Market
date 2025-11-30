package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.CartItemRepository;
import com.maono.marketapplication.repositories.util.Page;
import com.maono.marketapplication.util.ProductSortType;
import com.maono.marketapplication.repositories.ProductRepository;
import com.maono.marketapplication.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public Mono<Page<Product>> findByPage(String search, ProductSortType sort, int pageSize, int pageNumber) {
        String sortBy = switch (sort) {
            case ALPHA -> "title";
            case PRICE -> "price";
            case NO -> "";
        };

        Function<List<Product>, Mono<Page<Product>>> pageFunction = products ->
                productRepository
                        .totalCount()
                        .flatMap(totalCount -> {
                                        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                                        return Mono.just(new Page<>(
                                                products,
                                                pageSize,
                                                pageNumber,
                                                pageNumber < totalPages,
                                                pageNumber > 1,
                                                totalPages)
                                        );
                                    }
                                );

        Function<List<Product>, Mono<List<Product>>> cartItemMapFunction = products -> {
            List<Long> productsIds = products.stream().map(Product::getId).toList();
            return cartItemRepository.findAllById(productsIds)
                    .collectMap(CartItem::getId)
                    .flatMap(mapped -> Flux.fromIterable(products.stream()
                                    .peek(product -> {
                                        if (mapped.containsKey(product.getId())) {
                                            CartItem c = mapped.get(product.getId());
                                            c.setProduct(product);
                                            product.setCartItem(c);
                                        }
                                    })
                                    .toList())
                            .collectList());
        };

        int offset = (pageNumber - 1) * pageSize;
        if (search.isBlank()) {
            return productRepository.findProductsByPage(pageSize, offset, sortBy)
                    .collectList()
                    .flatMap(cartItemMapFunction)
                    .flatMap(pageFunction);
        } else {
            return productRepository.findProductsByPageAndTitle(search, pageSize, offset, sortBy)
                    .collectList()
                    .flatMap(cartItemMapFunction)
                    .flatMap(pageFunction);
        }
    }

    @Override
    public Mono<Product> findProductByIdWithRelations(Long id) {
        return productRepository.findById(id)
                .flatMap(product -> cartItemRepository.findById(product.getId())
                        .map(cartItem -> {
                            cartItem.setProduct(product);
                            product.setCartItem(cartItem);
                            return product;
                        })
                        .switchIfEmpty(Mono.just(product)));
    }

}
