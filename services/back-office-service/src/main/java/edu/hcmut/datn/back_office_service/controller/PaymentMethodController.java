package edu.hcmut.datn.back_office_service.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hcmut.datn.back_office_service.dao.PaymentMethod;
import edu.hcmut.datn.back_office_service.dto.request.PaymentMethodDTO;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.PaymentMethodService;

@Controller
@RequestMapping("/api/payment-method")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethod>> create(@RequestBody PaymentMethodDTO paymentMethodDTO) {
        try {
            PaymentMethod newPaymentMethod = paymentMethodService.create(paymentMethodDTO.toEntity());

            return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                    "Create payment method successfully", newPaymentMethod));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{paymentMethodId}")
    public ResponseEntity<ApiResponse<PaymentMethod>> read(@PathVariable Long paymentMethodId) {
        try {
            PaymentMethod paymentMethod = paymentMethodService.read(paymentMethodId);

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read payment method successfully", paymentMethod));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<PaymentMethod> paymentMethods = paymentMethodService.readAll(pageNum, pageSize);

        if (paymentMethods.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No Payment Method Exists", null));
        }

        return ResponseEntity.ok().body(
                ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all payment methods successfully", paymentMethods));
    }

    @PutMapping("/{paymentMethodId}")
    public ResponseEntity<ApiResponse<PaymentMethod>> update(@PathVariable Long paymentMethodId,
            @RequestBody PaymentMethodDTO paymentMethodDTO) {
        try {
            PaymentMethod paymentMethod = paymentMethodService.update(paymentMethodId, paymentMethodDTO.toEntity());

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update payment method successfully", paymentMethod));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long paymentMethodId) {
        try {
            paymentMethodService.delete(paymentMethodId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete payment method successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

}
