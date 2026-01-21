package com.maono.paymentservice.repositories;

import com.maono.paymentservice.model.AccountBalance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BalanceRepository {
    Mono<AccountBalance> save(AccountBalance accountBalance);
    Mono<AccountBalance> update(AccountBalance accountBalance);
    Mono<AccountBalance> getBalance();
    Flux<AccountBalance> findAll();
}
