package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.CartItemUseCase;
import microservice.base_source.persistence.dto.CartItemWithBatchDetailDTO;
import microservice.base_source.persistence.repository.CartItemRepository;
import microservice.base_source.presentation.response.cartitem.CartItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService implements CartItemUseCase {
    
    private final CartItemRepository cartItemRepository;
    
    @Override
    @Transactional
    public CartItem addToCart(Long cartId, String batchDetailId, Long quantity, Boolean isSelected) {
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndBatchDetailId(cartId, batchDetailId);
        
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (isSelected != null) {
                item.setIsSelected(isSelected);
            }
            return cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCartId(cartId);
            newItem.setBatchDetailId(batchDetailId);
            newItem.setQuantity(quantity);
            newItem.setIsSelected(isSelected != null ? isSelected : true);
            return cartItemRepository.save(newItem);
        }
    }
    
    @Override
    public CartItem get(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + cartItemId));
    }
    
    @Override
    public List<CartItem> getAllByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }
    
    @Override
    public List<CartItemResponse> getAllWithBatchDetailByCartId(Long cartId) {
        List<CartItemWithBatchDetailDTO> dtos = cartItemRepository
                .findCartItemsWithBatchDetailByCartId(cartId);
        
        return dtos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CartItem> getSelectedItems(Long cartId) {
        return cartItemRepository.findByCartIdAndIsSelected(cartId, true);
    }
    
    @Override
    public List<CartItemResponse> getSelectedItemsWithBatchDetail(Long cartId) {
        List<CartItemWithBatchDetailDTO> dtos = cartItemRepository
                .findSelectedCartItemsWithBatchDetailByCartId(cartId);
        
        return dtos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CartItem updateQuantity(Long cartItemId, Long quantity) {
        CartItem cartItem = get(cartItemId);
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
    
    @Override
    @Transactional
    public CartItem toggleSelection(Long cartItemId) {
        CartItem cartItem = get(cartItemId);
        cartItem.setIsSelected(!cartItem.getIsSelected());
        return cartItemRepository.save(cartItem);
    }
    
    @Override
    @Transactional
    public CartItem update(Long cartItemId, CartItem cartItem) {
        CartItem existingItem = get(cartItemId);
        
        if (cartItem.getQuantity() != null) {
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            existingItem.setQuantity(cartItem.getQuantity());
        }
        
        if (cartItem.getIsSelected() != null) {
            existingItem.setIsSelected(cartItem.getIsSelected());
        }
        
        return cartItemRepository.save(existingItem);
    }
    
    @Override
    @Transactional
    public void delete(Long cartItemId) {
        CartItem cartItem = get(cartItemId);
        cartItemRepository.delete(cartItem);
    }
    
    @Override
    @Transactional
    public void deleteAllByCartId(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }
    
    @Override
    @Transactional
    public void deleteSelectedItems(Long cartId) {
        cartItemRepository.deleteByCartIdAndIsSelected(cartId, true);
    }
    
    @Override
    @Transactional
    public void selectAll(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        items.forEach(item -> item.setIsSelected(true));
        cartItemRepository.saveAll(items);
    }
    
    @Override
    @Transactional
    public void deselectAll(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        items.forEach(item -> item.setIsSelected(false));
        cartItemRepository.saveAll(items);
    }
    
    // Helper method to convert DTO to Response
    private CartItemResponse convertToResponse(CartItemWithBatchDetailDTO dto) {
        BigDecimal unitPrice = dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));
        
        return CartItemResponse.builder()
                .cartItemId(dto.getCartItemId())
                .cartId(dto.getCartId())
                .batchDetailId(dto.getBatchDetailId())
                .quantity(dto.getQuantity())
                .isSelected(dto.getIsSelected())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .productName(dto.getProductName())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}