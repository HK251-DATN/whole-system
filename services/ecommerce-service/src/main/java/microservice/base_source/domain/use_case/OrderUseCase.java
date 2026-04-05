package microservice.base_source.domain.use_case;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.presentation.response.order.OrderDetailResponse;

public interface OrderUseCase {
    /**
     * <p> Search:
     * <p> 	- buyer_id or total price
     * <p> 	- note or status
     * <p> 	- createdAt
     * <p> Sort:
     * <p> 	- buyer_id asc/desc
     * <p> 	- total_price asc/desc
     * <p> 	- createdAt asc/desc
     * <p> 	- status
     * <p> Filter:
     * <p> 	- status
     * <p>  - total_price range
     * <p>  - createdAt range
     * @return danh sách order có phân trang
     */
    List<Order> search(
            String buyerId,
            String searchString,
            OrderStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDateTime minTime,
            LocalDateTime maxTime,
            String sortByStatus,
            String sortByPrice,
            String sortByTime,
            int page, int size);
    
    /**
     * <p> Condition create order
     * <p> 	- coupon: valid
     * <p> 	- product: in stock
     * <p> 	- buyer: exist
     * <p> 	- create order temp employee confirm
     * @return
     */
    Order create(Order order, List<OrderItem> orderItems);
    
    /**
     * Create order from cart (simplified flow)
     * @param buyerId - authenticated user ID
     * @param addressId - delivery address ID
     * @return created order
     */
    Order createFromCart(String buyerId, Long addressId);
    
    /**
     * Get all orders (with optional buyer filter)
     * @return List order
     */
    List<Order> getAll(String buyerId, int page, int size);
    
    /**
     * Get orders by buyer ID
     */
    List<Order> getByBuyerId(String buyerId, int page, int size);
    
    /**
     * Get order with order items
     */
    OrderDetailResponse getOrderDetail(Long orderId);
    
    /**
     * Get single order by ID
     */
    Order get(Long id);
    
    /**
     * Delete order
     */
    void delete(Long id);
}