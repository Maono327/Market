package com.maono.marketapplication.models.dto;

import java.math.BigDecimal;

public record ImportProduct(String title,
                            String description,
                            String imageName,
                            BigDecimal price) {
}
