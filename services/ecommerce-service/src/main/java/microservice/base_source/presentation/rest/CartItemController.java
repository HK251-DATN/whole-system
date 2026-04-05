package microservice.base_source.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.use_case.CartItemUseCase;
import microservice.base_source.domain.use_case.CartUseCase;
import microservice.base_source.infrastructure.security.AuthenticatedUser;
import microservice.base_source.presentation.request.CartItemAddRequest;
import microservice.base_source.presentation.request.CartItemUpdateRequest;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;
import microservice.base_source.presentation.response.global.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    
    private final CartItemUseCase cartItemUseCase;
    private final CartUseCase cartUseCase;
    
    /**
     * Add item to cart
     * POST /api/cart-items
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody CartItemAddRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            
            // Get user's cart
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            // Add item to cart
            CartItem cartItem = cartItemUseCase.addToCart(
                    cart.getCartId(),
                    request.getBatchDetailId(),
                    request.getQuantity(),
                    request.getIsSelected()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.CREATED.toString(),
                            "Item added to cart successfully",
                            CartItemResponse.fromEntity(cartItem)
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
     * Get all cart items for authenticated user WITH batch detail information
     * GET /api/cart-items
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItems(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            
            // Get user's cart
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            // Get all cart items with batch detail info
            List<CartItemResponse> responses = cartItemUseCase
                    .getAllWithBatchDetailByCartId(cart.getCartId());
            
            if (responses.isEmpty()) {
                return ResponseEntity.ok()
                        .body(ApiResponse.SKIP_AS_GOOD(
                                HttpStatus.OK.toString(),
                                "Cart is empty",
                                null
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Cart items retrieved successfully",
                            responses
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
     * Get selected cart items WITH batch detail information
     * GET /api/cart-items/selected
     */
    @GetMapping("/selected")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getSelectedItems(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            // Get selected items with batch detail info
            List<CartItemResponse> responses = cartItemUseCase
                    .getSelectedItemsWithBatchDetail(cart.getCartId());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Selected items retrieved successfully",
                            responses
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
     * Get a specific cart item
     * GET /api/cart-items/{cartItemId}
     */
    @GetMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> getCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            CartItem cartItem = cartItemUseCase.get(cartItemId);
            
            // Verify cart item belongs to user's cart
            if (!cartItem.getCartId().equals(cart.getCartId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied to this cart item",
                                null
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Cart item retrieved successfully",
                            CartItemResponse.fromEntity(cartItem)
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
     * Update cart item (quantity and/or selection)
     * PUT /api/cart-items/{cartItemId}
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            CartItem existingItem = cartItemUseCase.get(cartItemId);
            
            // Verify cart item belongs to user's cart
            if (!existingItem.getCartId().equals(cart.getCartId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied to this cart item",
                                null
                        ));
            }
            
            // Create update entity
            CartItem updateData = new CartItem();
            updateData.setQuantity(request.getQuantity());
            updateData.setIsSelected(request.getIsSelected());
            
            CartItem updated = cartItemUseCase.update(cartItemId, updateData);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Cart item updated successfully",
                            CartItemResponse.fromEntity(updated)
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
     * Update only quantity
     * PATCH /api/cart-items/{cartItemId}/quantity
     */
    @PatchMapping("/{cartItemId}/quantity")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Long quantity,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            CartItem existingItem = cartItemUseCase.get(cartItemId);
            
            // Verify ownership
            if (!existingItem.getCartId().equals(cart.getCartId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied",
                                null
                        ));
            }
            
            CartItem updated = cartItemUseCase.updateQuantity(cartItemId, quantity);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Quantity updated successfully",
                            CartItemResponse.fromEntity(updated)
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
     * Toggle selection status
     * PATCH /api/cart-items/{cartItemId}/toggle
     */
    @PatchMapping("/{cartItemId}/toggle")
    public ResponseEntity<ApiResponse<CartItemResponse>> toggleSelection(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            CartItem existingItem = cartItemUseCase.get(cartItemId);
            
            // Verify ownership
            if (!existingItem.getCartId().equals(cart.getCartId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied",
                                null
                        ));
            }
            
            CartItem updated = cartItemUseCase.toggleSelection(cartItemId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Selection toggled successfully",
                            CartItemResponse.fromEntity(updated)
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
     * Delete a cart item
     * DELETE /api/cart-items/{cartItemId}
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            CartItem existingItem = cartItemUseCase.get(cartItemId);
            
            // Verify ownership
            if (!existingItem.getCartId().equals(cart.getCartId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.ERROR(
                                HttpStatus.FORBIDDEN.toString(),
                                "Access denied",
                                null
                        ));
            }
            
            cartItemUseCase.delete(cartItemId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Cart item deleted successfully",
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
     * Clear all items from cart
     * DELETE /api/cart-items
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            cartItemUseCase.deleteAllByCartId(cart.getCartId());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Cart cleared successfully",
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
     * Select all items in cart
     * POST /api/cart-items/select-all
     */
    @PostMapping("/select-all")
    public ResponseEntity<ApiResponse<Void>> selectAll(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            cartItemUseCase.selectAll(cart.getCartId());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "All items selected",
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
     * Deselect all items in cart
     * POST /api/cart-items/deselect-all
     */
    @PostMapping("/deselect-all")
    public ResponseEntity<ApiResponse<Void>> deselectAll(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        
        try {
            String buyerId = principal.getId().toString();
            var cart = cartUseCase.getByBuyerId(buyerId);
            
            cartItemUseCase.deselectAll(cart.getCartId());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "All items deselected",
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