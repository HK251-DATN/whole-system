package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.Order;

public interface OrderService {
    Order create(Order order);

    Order read(Long orderId);

    List<Order> readAll(Integer pageNum, Integer pageSize);

    Order update(Long orderId, Order order);

    void delete(Long orderId);

    void empConfirmOrder(Long orderId, Long empId);
    
    void empPackageOrder(Long orderId, Long empId);
    
    void empShipOrder(Long orderId, Long empId);
    
}
