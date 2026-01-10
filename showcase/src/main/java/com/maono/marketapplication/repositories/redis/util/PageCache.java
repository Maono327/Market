package com.maono.marketapplication.repositories.redis.util;

import com.maono.marketapplication.models.Product;

import java.util.List;

public record PageCache(List<Product> products,
                        int pageSize,
                        int pageNumber,
                        boolean hasNext,
                        boolean hasPrevious,
                        int totalPages) {
}
