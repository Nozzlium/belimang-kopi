package com.kopi.belimang.merchant.exception;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(String message) {
        super(message);
    }

    public MerchantNotFoundException(Long merchantId) {
        super("Merchant with id " + merchantId + " not found");
    }
}