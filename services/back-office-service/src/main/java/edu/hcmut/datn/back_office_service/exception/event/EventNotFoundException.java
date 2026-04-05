package edu.hcmut.datn.back_office_service.exception.event;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
