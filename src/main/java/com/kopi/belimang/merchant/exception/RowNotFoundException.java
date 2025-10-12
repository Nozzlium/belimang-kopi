package com.kopi.belimang.merchant.exception;

public class RowNotFoundException extends RuntimeException {
    public RowNotFoundException(String message) {
        super(message);
    }

    public RowNotFoundException(Long merchantId) {
        super("Merchant with id " + merchantId + " not found");
    }
}