package com.maono.paymentservice.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}
