package com.maono.paymentservice.services.impl;

import com.maono.paymentservice.exceptions.BalanceNotFoundException;
import com.maono.paymentservice.exceptions.InsufficientFundsException;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.repositories.BalanceRepository;
import com.maono.paymentservice.services.BalanceOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceOperationServiceImpl implements BalanceOperationService {
    private final BalanceRepository balanceRepository;

    @Override
    public Mono<AccountBalance> saveBalance(AccountBalance balance) {
        return balanceRepository.save(balance);
    }

    @Override
    public Mono<AccountBalance> getBalance() {
        return balanceRepository.getBalance();
    }

    @Override
    public Mono<AccountBalance> doPayment(BigDecimal payment) {
        if (payment == null || payment.signum() <= 0) {
            return Mono.error(new IllegalArgumentException("Сумма должна быть больше 0"));
        }

        return balanceRepository.getBalance()
                .switchIfEmpty(Mono.error(new BalanceNotFoundException()))
                .flatMap(current -> {
                    current.reduce(payment);
                    if (current.getAccountBalance().signum() < 0) {
                        return Mono.error(new InsufficientFundsException());
                    }

                     return balanceRepository.update(current);
                });
    }
}
