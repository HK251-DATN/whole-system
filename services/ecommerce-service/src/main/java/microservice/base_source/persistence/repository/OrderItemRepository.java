package microservice.base_source.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    /**
     * Find all order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Delete all order items by order ID
     */
    void deleteByOrderId(Long orderId);
}
