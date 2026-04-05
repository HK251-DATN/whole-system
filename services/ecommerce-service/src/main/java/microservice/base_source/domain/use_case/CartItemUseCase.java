package microservice.base_source.domain.use_case;

import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;

import java.util.List;

public interface CartItemUseCase {
    
    /**
     * Add item to cart or update quantity if already exists
     */
    CartItem addToCart(Long cartId, String batchDetailId, Long quantity, Boolean isSelected);
    
    /**
     * Get a single cart item by ID
     */
    CartItem get(Long cartItemId);
    
    /**
     * Get all cart items for a specific cart (basic info only)
     */
    List<CartItem> getAllByCartId(Long cartId);
    
    /**
     * Get all cart items with batch detail information
     */
    List<CartItemResponse> getAllWithBatchDetailByCartId(Long cartId);
    
    /**
     * Get selected cart items (basic info only)
     */
    List<CartItem> getSelectedItems(Long cartId);
    
    /**
     * Get selected cart items with batch detail information
     */
    List<CartItemResponse> getSelectedItemsWithBatchDetail(Long cartId);
    
    /**
     * Update cart item quantity
     */
    CartItem updateQuantity(Long cartItemId, Long quantity);
    
    /**
     * Toggle selection status of cart item
     */
    CartItem toggleSelection(Long cartItemId);
    
    /**
     * Update cart item (quantity and/or selection)
     */
    CartItem update(Long cartItemId, CartItem cartItem);
    
    /**
     * Delete a cart item
     */
    void delete(Long cartItemId);
    
    /**
     * Delete all items in a cart
     */
    void deleteAllByCartId(Long cartId);
    
    /**
     * Delete selected items in a cart
     */
    void deleteSelectedItems(Long cartId);
    
    /**
     * Select all items in cart
     */
    void selectAll(Long cartId);
    
    /**
     * Deselect all items in cart
     */
    void deselectAll(Long cartId);
}