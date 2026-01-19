package com.maono.paymentservice.repositories;

import com.maono.paymentservice.model.AccountBalance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface BalanceRepository extends R2dbcRepository<AccountBalance, Long> {
}
