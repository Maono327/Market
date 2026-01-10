package com.maono.marketapplication.repositories.redis;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import reactor.core.publisher.Mono;

public interface RedisPageRepository {
    Mono<Page<Product>> cachePage(Page<Product> page,
                              int totalPages,
                              int pageNumber,
                              int pageSize,
                              String search,
                              String sortBy);

    Mono<PageCache> getCachedPage(String search,
                                  int pageSize,
                                  int pageNumber,
                                  String sortBy);
}
