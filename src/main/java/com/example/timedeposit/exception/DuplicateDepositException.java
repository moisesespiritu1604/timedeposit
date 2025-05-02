package com.example.timedeposit.exception;

public class DuplicateDepositException extends RuntimeException{
    public DuplicateDepositException(String message) {
        super(message);
    }
}
