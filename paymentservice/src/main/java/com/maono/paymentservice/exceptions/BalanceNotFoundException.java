package com.maono.paymentservice.exceptions;

public class BalanceNotFoundException extends RuntimeException {
    public BalanceNotFoundException() {
    }

    public BalanceNotFoundException(String message) {
        super(message);
    }
}
