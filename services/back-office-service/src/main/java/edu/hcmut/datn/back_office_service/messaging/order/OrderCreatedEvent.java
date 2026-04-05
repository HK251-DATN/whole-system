package edu.hcmut.datn.back_office_service.messaging.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hcmut.datn.back_office_service.common.enums.OrderStatus;
import edu.hcmut.datn.back_office_service.dao.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    
    @Getter
    private Long orderId;
    
    @Getter
    private String buyerId;    // Java field name
    
    @Getter
    private Long totalPrice;
    
    public Order toOrderEntity() {
        Order newOrder = new Order();
        newOrder.setOrderId(orderId);
        newOrder.setStatus(OrderStatus.CREATED);
        newOrder.setOwnedBy(Long.valueOf(buyerId));
        return newOrder;
    }
}
