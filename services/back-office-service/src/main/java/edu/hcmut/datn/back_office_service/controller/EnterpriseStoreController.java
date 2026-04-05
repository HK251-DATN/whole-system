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

import edu.hcmut.datn.back_office_service.dao.EnterpriseStore;
import edu.hcmut.datn.back_office_service.dto.request.EnterpriseStoreCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.EnterpriseStoreUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.EnterpriseStoreService;

@Controller
@RequestMapping("/api/enterprise-store")
@RequiredArgsConstructor
public class EnterpriseStoreController {

    private final EnterpriseStoreService enterpriseStoreService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnterpriseStore>> create(
            @RequestBody EnterpriseStoreCreateRequest createRequest) {
        try {
            EnterpriseStore store = enterpriseStoreService.create(createRequest.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create enterprise store successfully", store));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<EnterpriseStore>> read(@PathVariable Long storeId) {
        try {
            EnterpriseStore store = enterpriseStoreService.read(storeId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read enterprise store successfully", store));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnterpriseStore>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<EnterpriseStore> stores = enterpriseStoreService.readAll(pageNum, pageSize);

        if (stores.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No enterprise store found", null));
        }
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read enterprise stores successfully", stores));
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<EnterpriseStore>> update(@PathVariable Long storeId,
            @RequestBody EnterpriseStoreUpdateRequest updateRequest) {
        try {
            EnterpriseStore store = enterpriseStoreService.update(storeId, updateRequest.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update enterprise store successfully", store));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long storeId) {
        try {
            enterpriseStoreService.delete(storeId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete enterprise store successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
