package com.maono.marketapplication.models.dto.responses;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderDto(Long id,
                       List<OrderItemDto> items,
                       BigDecimal totalSum) {
}
