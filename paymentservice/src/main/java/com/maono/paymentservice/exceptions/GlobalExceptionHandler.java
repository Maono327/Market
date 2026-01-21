package com.maono.paymentservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(BalanceNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(BalanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Void> handleConflict(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}

