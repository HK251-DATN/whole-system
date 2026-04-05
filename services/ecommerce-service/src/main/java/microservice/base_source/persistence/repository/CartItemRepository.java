package microservice.base_source.persistence.repository;

import microservice.base_source.domain.entity.CartItem;
import microservice.base_source.persistence.dto.CartItemWithBatchDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find all cart items by cart ID
    List<CartItem> findByCartId(Long cartId);
    
    // Find cart items by cart ID and selection status
    List<CartItem> findByCartIdAndIsSelected(Long cartId, Boolean isSelected);
    
    // Find a specific cart item by cart ID and batch detail ID
    Optional<CartItem> findByCartIdAndBatchDetailId(Long cartId, String batchDetailId);
    
    // Count cart items in a cart
    Long countByCartId(Long cartId);
    
    // Count selected cart items
    Long countByCartIdAndIsSelected(Long cartId, Boolean isSelected);
    
    // Delete all cart items by cart ID
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
    
    // Delete selected cart items
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.isSelected = :isSelected")
    void deleteByCartIdAndIsSelected(@Param("cartId") Long cartId, @Param("isSelected") Boolean isSelected);
    
    // Check if cart item exists
    boolean existsByCartIdAndBatchDetailId(Long cartId, String batchDetailId);
    
    // Get cart items with batch detail information (for display purposes)
    @Query(value = """
        SELECT
            ci.cart_item_id AS cartItemId,
            ci.cart_id AS cartId,
            ci.batch_detail_id AS batchDetailId,
            ci.quantity AS quantity,
            ci.is_selected AS isSelected,
            ci.created_at AS createdAt,
            ci.updated_at AS updatedAt,
            pg.name AS productName,
            bd.price AS unitPrice
        FROM CART_ITEM ci
        LEFT JOIN BATCH_DETAIL bd ON ci.batch_detail_id = bd.batch_detail_id
        LEFT JOIN PRODUCT_GENERAL pg ON bd.product_general_id = pg.product_general_id
        WHERE ci.cart_id = :cartId
        ORDER BY ci.created_at DESC
        """, nativeQuery = true)
    List<CartItemWithBatchDetailDTO> findCartItemsWithBatchDetailByCartId(@Param("cartId") Long cartId);
    
    // Get selected cart items with batch detail information
    @Query(value = """
        SELECT
            ci.cart_item_id AS cartItemId,
            ci.cart_id AS cartId,
            ci.batch_detail_id AS batchDetailId,
            ci.quantity AS quantity,
            ci.is_selected AS isSelected,
            ci.created_at AS createdAt,
            ci.updated_at AS updatedAt,
            pg.name AS productName,
            bd.price AS unitPrice
        FROM CART_ITEM ci
        LEFT JOIN BATCH_DETAIL bd ON ci.batch_detail_id = bd.batch_detail_id
        LEFT JOIN PRODUCT_GENERAL pg ON bd.product_general_id = pg.product_general_id
        WHERE ci.cart_id = :cartId AND ci.is_selected = true
        ORDER BY ci.created_at DESC
        """, nativeQuery = true)
    List<CartItemWithBatchDetailDTO> findSelectedCartItemsWithBatchDetailByCartId(@Param("cartId") Long cartId);
}