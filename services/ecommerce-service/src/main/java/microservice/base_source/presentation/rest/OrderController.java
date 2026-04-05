package microservice.base_source.presentation.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.exception.type.UnauthorizedException;
import microservice.base_source.infrastructure.security.AuthenticatedUser;
import microservice.base_source.presentation.request.CreateOrderFromCartRequest;
import microservice.base_source.presentation.response.order.OrderDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.use_case.OrderUseCase;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    @Autowired
    private OrderUseCase orderUseCase;
    
    /**
     * Create order from cart
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrderFromCart(
            @RequestBody @Valid CreateOrderFromCartRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            String buyerId = principal.getId().toString();
            
            Order order = orderUseCase.createFromCart(buyerId, request.getAddressId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.CREATED.toString(),
                            "Order created successfully",
                            order
                    ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(
                            HttpStatus.NOT_FOUND.toString(),
                            e.getMessage(),
                            null
                    ));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    ));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ERROR(
                            HttpStatus.UNAUTHORIZED.toString(),
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(
                            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "An error occurred while creating the order",
                            null
                    ));
        }
    }
    
    /**
     * Get order detail with order items
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            String buyerId = principal.getId().toString();
            
            // Get order detail with items
            OrderDetailResponse orderDetail = orderUseCase.getOrderDetail(id);
            
            // Verify order belongs to user
            if (!orderDetail.getBuyerId().equals(buyerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied to this order",
                                null
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Get order detail success",
                            orderDetail
                    ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(
                            HttpStatus.NOT_FOUND.toString(),
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * Get all orders for authenticated user
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            String buyerId = principal.getId().toString();
            List<Order> orders = orderUseCase.getByBuyerId(buyerId, page, size);
            
            if (orders.isEmpty()) {
                return ResponseEntity.ok()
                        .body(ApiResponse.SKIP_AS_GOOD(
                                HttpStatus.OK.toString(),
                                "No orders found",
                                null
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Get all orders success",
                            orders
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * Search/filter orders for authenticated user
     * GET /api/orders/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Order>>> search(
            @RequestParam(defaultValue = "") String searchString,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") BigDecimal minPrice,
            @RequestParam(defaultValue = "0") BigDecimal maxPrice,
            @RequestParam(required = false) LocalDateTime minTime,
            @RequestParam(required = false) LocalDateTime maxTime,
            @RequestParam(defaultValue = "") String sortByStatus,
            @RequestParam(defaultValue = "DESC") String sortByPrice,
            @RequestParam(defaultValue = "DESC") String sortByTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            String buyerId = principal.getId().toString();
            
            List<Order> orders = orderUseCase.search(
                    buyerId,
                    searchString,
                    status,
                    minPrice,
                    maxPrice,
                    minTime,
                    maxTime,
                    sortByStatus,
                    sortByPrice,
                    sortByTime,
                    page,
                    size
            );
            
            if (orders.isEmpty()) {
                return ResponseEntity.ok()
                        .body(ApiResponse.SKIP_AS_GOOD(
                                HttpStatus.OK.toString(),
                                "No orders found",
                                null
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Search orders success",
                            orders
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * Cancel order (soft delete - change status to CANCELLED)
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            String buyerId = principal.getId().toString();
            Order order = orderUseCase.get(id);
            
            // Verify order belongs to user
            if (!order.getBuyerId().equals(buyerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied to this order",
                                null
                        ));
            }
            
            // Only allow cancellation if order is still PENDING
            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(
                                HttpStatus.BAD_REQUEST.toString(),
                                "Cannot cancel order with status: " + order.getStatus(),
                                null
                        ));
            }
            
            orderUseCase.delete(id);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Order cancelled successfully",
                            null
                    ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(
                            HttpStatus.NOT_FOUND.toString(),
                            e.getMessage(),
                            null
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    ));
        }
    }
}