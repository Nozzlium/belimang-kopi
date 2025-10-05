package com.kopi.belimang.auth.exceptions;

public class DuplicateCredentialException extends RuntimeException {
    public DuplicateCredentialException(String message) {
        super(message);
    }
}
