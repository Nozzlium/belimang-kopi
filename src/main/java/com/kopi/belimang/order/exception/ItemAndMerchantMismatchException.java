package com.kopi.belimang.order.exception;

public class ItemAndMerchantMismatchException extends RuntimeException {
    public ItemAndMerchantMismatchException(String message) {
        super(message);
    }

    public ItemAndMerchantMismatchException(Long itemId, Long merchantId) {
        super("Item with id " + itemId + " does not belong to merchant with id " + merchantId);
    }
}
