package edu.hcmut.datn.back_office_service.exception.productrequest;

public class ProductRequestNotFoundException extends RuntimeException {
    public ProductRequestNotFoundException(String message) {
        super(message);
    }
}
