package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.OrderStatus;
import edu.hcmut.datn.back_office_service.dao.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    private OrderStatus status;
    private Long confirmedBy;
    private Long packagedBy;
    private Long shippedBy;

    public Order toEntity() {
        return new Order(status, confirmedBy, packagedBy, shippedBy);
    }
}
