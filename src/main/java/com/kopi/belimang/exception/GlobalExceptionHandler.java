package com.kopi.belimang.exception;

import com.kopi.belimang.auth.exceptions.DuplicateCredentialException;
import com.kopi.belimang.merchant.exception.MerchantNotFoundException;
import com.kopi.belimang.order.exception.ItemAndMerchantMismatchException;
import com.kopi.belimang.order.exception.MerchantTooFarException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()) );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> error = Map.of(
                "message", "Request body is missing or invalid. Please provide a valid JSON body."
        );
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceException(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMerchantNotFoundException(MerchantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Merchant not found skibidi"));
    }

    @ExceptionHandler(ItemAndMerchantMismatchException.class)
    public ResponseEntity<ErrorResponse> handleItemAndMerchantMismatchException(ItemAndMerchantMismatchException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MerchantTooFarException.class)
    public ResponseEntity<ErrorResponse> handleMerchantTooFar(MerchantTooFarException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Customize the response as needed
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ErrorResponse("Internal Server Belimang"));
    }

    @ExceptionHandler(DuplicateCredentialException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCredentialException(DuplicateCredentialException ex) {
        return ResponseEntity.status(HttpStatus.valueOf(409)).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCredentialException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.valueOf(400)).body(new ErrorResponse(ex.getMessage()));
    }
}