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

import edu.hcmut.datn.back_office_service.dao.DemandResponse;
import edu.hcmut.datn.back_office_service.dto.request.DemandResponseCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.DemandResponseUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.DemandResponseService;

@Controller
@RequestMapping("/api/demand-response")
@RequiredArgsConstructor
public class DemandResponseController {

    private final DemandResponseService demandResponseService;

    @PostMapping
    public ResponseEntity<ApiResponse<DemandResponse>> create(@RequestBody DemandResponseCreateRequest createRequest) {
        try {
            DemandResponse demandResponse = demandResponseService.create(createRequest.toEntity());

            return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                    "Create demand response successfully", demandResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{demandResponseId}")
    public ResponseEntity<ApiResponse<DemandResponse>> read(@PathVariable Long demandResponseId) {
        try {
            DemandResponse demandResponse = demandResponseService.read(demandResponseId);

            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read demand response successfully", demandResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandResponse>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<DemandResponse> demandResponses = demandResponseService.readAll(pageNum, pageSize);

        if (demandResponses.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No demand response found", null));
        }

        return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                "Read all demand response successfully", demandResponses));
    }

    @PutMapping("/{demandResponseId}")
    public ResponseEntity<ApiResponse<DemandResponse>> update(@PathVariable Long demandResponseId,
            @RequestBody DemandResponseUpdateRequest updateRequest) {
        try {
            DemandResponse demandResponse = demandResponseService.update(demandResponseId, updateRequest.toEntity());

            return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                    "Update demand response successfully", demandResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{demandResponseId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long demandResponseId) {
        try {
            demandResponseService.delete(demandResponseId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete demand response successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

}
