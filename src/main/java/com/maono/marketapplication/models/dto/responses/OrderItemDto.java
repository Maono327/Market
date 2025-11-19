package com.maono.marketapplication.models.dto.responses;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemDto(Long id,
                           String title,
                           BigDecimal price,
                           int count) {
}
