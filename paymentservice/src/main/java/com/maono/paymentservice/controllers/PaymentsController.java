package com.maono.paymentservice.controllers;

import com.maono.paymentservice.api.PaymentApi;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.services.BalanceOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentsController implements PaymentApi {

    private final BalanceOperationService balanceOperationService;

    @Override
    public Mono<ResponseEntity<BigDecimal>> balance(ServerWebExchange exchange) {
        return balanceOperationService.getBalance()
                .map(AccountBalance::getAccountBalance)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> buy(BigDecimal sum, ServerWebExchange exchange) {
        return balanceOperationService.doPayment(sum)
                .thenReturn(ResponseEntity.ok().build());
    }
}

