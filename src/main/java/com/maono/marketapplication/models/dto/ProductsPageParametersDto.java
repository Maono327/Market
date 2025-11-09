package com.maono.marketapplication.models.dto;

public record ProductsPageParametersDto(int pageSize,
                                        int pageNumber,
                                        boolean hasNext,
                                        boolean hasPrevious) {
}
