package edu.hcmut.datn.back_office_service.exception.paymentmethod;

public class PaymentMethodNotFoundException extends RuntimeException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
}
