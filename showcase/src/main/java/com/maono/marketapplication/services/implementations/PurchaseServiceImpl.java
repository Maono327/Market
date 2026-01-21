package com.maono.marketapplication.services.implementations;

import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.InsufficientFundsException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailable;
import com.maono.marketapplication.paymentservice.client.api.PaymentApi;
import com.maono.marketapplication.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {
    private final PaymentApi paymentApi;

    @Override
    public Mono<BigDecimal> getBalance() {
        return paymentApi.balance()
                .onErrorMap(this::mapPaymentServiceError);
    }

    @Override
    public Mono<Void> doPayment(BigDecimal payment) {
        return paymentApi.buy(payment)
                .onErrorMap(this::mapPaymentServiceError);
    }

    private Throwable mapPaymentServiceError(Throwable ex) {
        if (ex instanceof WebClientRequestException) {
            return new PurchaseServiceUnavailable("Сервис платежей не доступен");
        }

        Integer status = extractStatus(ex);
        if (status == null) {
            return ex;
        }

        return switch (status) {
            case 400 -> new IllegalArgumentException("Некорректная сумма");
            case 404 -> new BalanceNotFoundException("Баланс не найден");
            case 409 -> new InsufficientFundsException("Недостаточно средств");
            case 503 -> new PurchaseServiceUnavailable("Сервис платежей не доступен");
            default -> ex;
        };
    }

    private Integer extractStatus(Throwable ex) {
        if (ex instanceof WebClientResponseException webClientResponseException) {
            return webClientResponseException.getRawStatusCode();
        }

        return null;
    }
}

