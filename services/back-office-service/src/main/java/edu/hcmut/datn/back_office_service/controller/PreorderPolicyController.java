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

import edu.hcmut.datn.back_office_service.dao.PreorderPolicy;
import edu.hcmut.datn.back_office_service.dto.request.PreorderPolicyCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.PreorderPolicyUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.PreorderPolicyService;

@Controller
@RequestMapping("/api/preorder-policy")
@RequiredArgsConstructor
public class PreorderPolicyController {

    private final PreorderPolicyService preorderPolicyService;

    @PostMapping
    public ResponseEntity<ApiResponse<PreorderPolicy>> create(@RequestBody PreorderPolicyCreateRequest request) {
        try {
            PreorderPolicy policy = preorderPolicyService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create preorder policy successfully", policy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<ApiResponse<PreorderPolicy>> read(@PathVariable Long policyId) {
        try {
            PreorderPolicy policy = preorderPolicyService.read(policyId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read preorder policy successfully", policy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PreorderPolicy>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<PreorderPolicy> list = preorderPolicyService.readAll(pageNum, pageSize);

        if (list.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No Preorder Policy Exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all preorder policies successfully", list));
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<ApiResponse<PreorderPolicy>> update(@PathVariable Long policyId,
            @RequestBody PreorderPolicyUpdateRequest request) {
        try {
            PreorderPolicy updated = preorderPolicyService.update(policyId, request.toEntity());

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update preorder policy successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long policyId) {
        try {
            preorderPolicyService.delete(policyId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete preorder policy successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
