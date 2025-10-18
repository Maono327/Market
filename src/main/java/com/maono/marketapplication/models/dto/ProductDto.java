package com.maono.marketapplication.models.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductDto(long id,
                         String title,
                         String description,
                         String imgPath,
                         BigDecimal price,
                         long count) {
}
