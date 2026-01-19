package com.maono.marketapplication.services;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PurchaseService {
    Mono<BigDecimal> getBalance();
    Mono<Void> doPayment(BigDecimal payment);
}
