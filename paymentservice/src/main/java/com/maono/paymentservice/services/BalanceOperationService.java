package com.maono.paymentservice.services;

import com.maono.paymentservice.model.AccountBalance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BalanceOperationService {
    Mono<AccountBalance> saveBalance(AccountBalance balance);
    Mono<AccountBalance> doPayment(BigDecimal payment);
    Mono<AccountBalance> getBalance();
}
