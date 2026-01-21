package com.maono.marketapplication.exceptions;

public class PurchaseServiceUnavailable extends RuntimeException {
    public PurchaseServiceUnavailable(String message) {
        super(message);
    }
}
