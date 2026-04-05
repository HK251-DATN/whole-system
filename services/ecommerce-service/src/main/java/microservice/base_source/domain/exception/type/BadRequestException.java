package microservice.base_source.domain.exception.type;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}