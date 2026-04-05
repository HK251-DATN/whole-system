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

import edu.hcmut.datn.back_office_service.dao.Order;
import edu.hcmut.datn.back_office_service.dto.request.OrderCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.OrderUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.OrderService;

@Controller
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody OrderCreateRequest request) {
        try {
            Order newOrder = orderService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create order successfully", newOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> read(@PathVariable Long orderId) {
        try {
            Order order = orderService.read(orderId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read order successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<Order> orders = orderService.readAll(pageNum, pageSize);

        if (orders.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No Order Exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all orders successfully", orders));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable Long orderId,
            @RequestBody OrderUpdateRequest request) {
        try {
            Order updated = orderService.update(orderId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update order successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long orderId) {
        try {
            orderService.delete(orderId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete order successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PutMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOrder() {
        // Temp
        return null;
    }

    @PutMapping("/package")
    public ResponseEntity<ApiResponse<Void>> packageOrder() {
        // Temp
        return null;
    }

    @PutMapping("/ship")
    public ResponseEntity<ApiResponse<Void>> shipOrder() {
        // Temp
        return null;
    }
}
