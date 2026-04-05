package microservice.base_source.domain.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import microservice.base_source.domain.exception.type.CategoryNotFoundException;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.exception.type.ProductNotFoundException;
import microservice.base_source.domain.exception.type.WarnException;
import microservice.base_source.presentation.response.global.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
	// Exception not define
    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
        String guide = "Please contact the system administrator.";
        ApiResponse<String> response = ApiResponse.ERROR(
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "Internal Server Error",
                guide);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

	// Define Not validation dependency exxception
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public <T> ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        // Lấy danh sách lỗi cụ thể cho từng field
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Map<String, String>> response = ApiResponse.WARN(
                HttpStatus.BAD_REQUEST.toString(),
                "BAD REQUEST",
                fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

	// Define ProductNotFoundException
    @ExceptionHandler(ProductNotFoundException.class)
    public <T> ResponseEntity<ApiResponse<String>> handleOtherException(ProductNotFoundException ex) {
        String guide = "Please check your request again";
        ApiResponse<String> response = ApiResponse.WARN(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getMessage(),
                guide);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Define CategoryNotFoundException
    @ExceptionHandler(CategoryNotFoundException.class)
    public <T> ResponseEntity<ApiResponse<String>> handleOtherException(CategoryNotFoundException ex) {
        String guide = "Please check your request again";
        ApiResponse<String> response = ApiResponse.WARN(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getMessage(),
                guide);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Define NotFoundException
    @ExceptionHandler(NotFoundException.class)
    public <T> ResponseEntity<ApiResponse<String>> handleOtherException(NotFoundException ex) {
        String guide = "Please check your request again";
        ApiResponse<String> response = ApiResponse.WARN(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getMessage(),
                guide);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Define WarnException
    @ExceptionHandler(WarnException.class)
    public <T> ResponseEntity<ApiResponse<String>> handleOtherException(WarnException ex) {
        String guide = "Please check your request again";
        ApiResponse<String> response = ApiResponse.WARN(
                HttpStatus.BAD_REQUEST.toString(),
                ex.getMessage(),
                guide);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
