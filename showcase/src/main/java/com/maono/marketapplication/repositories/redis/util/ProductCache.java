package com.maono.marketapplication.repositories.redis.util;

import java.math.BigDecimal;

public record ProductCache(Long id,
                           String title,
                           String description,
                           String imageName,
                           BigDecimal price) {
}
