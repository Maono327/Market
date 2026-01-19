package com.maono.paymentservice.util;

import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.repositories.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "balance.filler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class BalanceDataFiller implements ApplicationRunner {
    private final BalanceRepository balanceRepository;
    @Value("${balance:0}")
    private BigDecimal balance;

    @Override
    public void run(ApplicationArguments args) {
        balanceRepository.findAll().switchIfEmpty(balanceRepository.save(new AccountBalance(balance))).then().block();
    }
}
