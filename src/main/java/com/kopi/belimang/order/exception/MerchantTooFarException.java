package com.kopi.belimang.order.exception;

public class MerchantTooFarException extends RuntimeException {
    public MerchantTooFarException(String message) {
        super(message);
    }

    public MerchantTooFarException(Long merchantId, double distance, double maxDistance) {
        super("Merchant with id " + merchantId + " is too far: " + distance + " meters (max: " + maxDistance + " meters)");
    }
}
