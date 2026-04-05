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

import edu.hcmut.datn.back_office_service.dao.Provider;
import edu.hcmut.datn.back_office_service.dto.request.ProviderCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.ProviderUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.ProviderService;

@Controller
@RequestMapping("/api/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Provider>> create(@RequestBody ProviderCreateRequest providerCreateRequest) {
        try {
            Provider newProvider = providerService.create(providerCreateRequest.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create provider successfully", newProvider));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ApiResponse<Provider>> read(@PathVariable Long providerId) {
        try {
            Provider provider = providerService.read(providerId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read provider successfully", provider));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Provider>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<Provider> providers = providerService.readAll(pageNum, pageSize);

        if (providers.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No provider found", null));
        }
        return ResponseEntity.ok()
                .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "Read all providers successfully", providers));
    }

    @PutMapping("/{providerId}")
    public ResponseEntity<ApiResponse<Provider>> update(@PathVariable Long providerId,
            @RequestBody ProviderUpdateRequest providerUpdateRequest) {
        try {
            Provider updatedProvider = providerService.update(providerId, providerUpdateRequest.toEntity());

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update provider successfully", updatedProvider));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("{providerId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long providerId) {
        try {
            providerService.delete(providerId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete provider successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

}
