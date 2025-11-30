package com.maono.marketapplication.services.util;

import com.maono.marketapplication.util.ProductActionType;
import reactor.core.publisher.Mono;

public interface Strategy {
    ProductActionType getType();
    Mono<Void> executeChange(Long id);
}
