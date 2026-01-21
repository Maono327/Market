package com.maono.paymentservice.repositories.impl;

import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.repositories.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BalanceRepositoryImpl implements BalanceRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<AccountBalance> save(AccountBalance accountBalance) {
        return r2dbcEntityTemplate.insert(accountBalance);
    }

    @Override
    public Mono<AccountBalance> update(AccountBalance accountBalance) {
        return r2dbcEntityTemplate
                .update(AccountBalance.class)
                .inTable("balance")
                .apply(Update.update("balance", accountBalance.getAccountBalance()))
                .then(getBalance());
    }

    @Override
    public Mono<AccountBalance> getBalance() {
        return r2dbcEntityTemplate.select(AccountBalance.class).one();
    }

    @Override
    public Flux<AccountBalance> findAll() {
        return r2dbcEntityTemplate.select(AccountBalance.class).all();
    }
}
