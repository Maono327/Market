package com.maono.marketapplication.repositories.util;

import java.util.List;

public record Page<T>(List<T> items,
                      int pageSize,
                      int pageNumber,
                      boolean hasNext,
                      boolean hasPrevious,
                      int totalPages) {
}