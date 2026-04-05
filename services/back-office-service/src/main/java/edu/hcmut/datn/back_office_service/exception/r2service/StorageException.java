package edu.hcmut.datn.back_office_service.exception.r2service;

public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}