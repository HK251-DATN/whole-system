package edu.hcmut.datn.back_office_service.controller;

import java.util.List;

import edu.hcmut.datn.back_office_service.repository.projection.BuyerUserProjection;
import edu.hcmut.datn.back_office_service.security.portable.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dto.request.BuyerCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.BuyerUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.BuyerService;

@Controller
@RequestMapping("/api/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    
    // User endpoints
    @GetMapping
    public ResponseEntity<ApiResponse<BuyerUserProjection>> userRead(@AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            Long requesterId = principal.getId();
            
            BuyerUserProjection buyer = buyerService.readBuyerInfo(requesterId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read buyer successfully", buyer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Buyer>> create(@RequestBody BuyerCreateRequest buyerCreateRequest) {
        try {
            Buyer newBuyer = buyerService.create(buyerCreateRequest.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create buyer successfully", newBuyer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    // Admin endpoints
    @GetMapping("/admin/{buyerId}")
    public ResponseEntity<ApiResponse<Buyer>> read(@PathVariable Long buyerId) {
        try {
            Buyer buyer = buyerService.read(buyerId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read buyer successfully", buyer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

//    @GetMapping("/admin")
//    public ResponseEntity<ApiResponse<List<Buyer>>> readAll(
//            @RequestParam(defaultValue = "1") Integer pageNum,
//            @RequestParam(defaultValue = "20") Integer pageSize) {
//        List<Buyer> buyers = buyerService.readAll(pageNum, pageSize);
//
//        if (buyers.isEmpty()) {
//            return ResponseEntity.ok()
//                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No provider found", null));
//        }
//        return ResponseEntity.ok()
//                .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "Read all buyers successfully", buyers));
//    }

    @PutMapping("/admin/{buyerId}")
    public ResponseEntity<ApiResponse<Buyer>> update(@PathVariable Long buyerId,
            @RequestBody BuyerUpdateRequest buyerUpdateRequest) {
        try {
            Buyer buyer = buyerService.update(buyerId, buyerUpdateRequest.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "update buyer successfully", buyer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/admin/{buyerId}")
    public ResponseEntity<ApiResponse<Buyer>> delete(@PathVariable Long buyerId) {
        try {
            buyerService.delete(buyerId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete buyer successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    // Admin get user info list
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<BuyerUserProjection>>> readAllBuyers () {
        List<BuyerUserProjection> buyers = buyerService.readBuyersInfo();
        
        if (buyers.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No provider found", null));
        }
        return ResponseEntity.ok()
                .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "Read all buyers info successfully", buyers));
    }
    
    
}
