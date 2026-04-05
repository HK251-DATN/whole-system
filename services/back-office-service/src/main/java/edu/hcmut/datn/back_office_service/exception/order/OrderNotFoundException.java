package edu.hcmut.datn.back_office_service.exception.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

}
