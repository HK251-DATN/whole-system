package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Order;
import edu.hcmut.datn.back_office_service.exception.order.OrderNotFoundException;
import edu.hcmut.datn.back_office_service.repository.OrderRepository;
import edu.hcmut.datn.back_office_service.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order read(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order Not Found"));
    }

    @Override
    public List<Order> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Order> page = orderRepository.findAll(pageable);

        return page.toList();
    }

    @Override
    public Order update(Long orderId, Order order) {
        Order cur = read(orderId);

        if (order.getStatus() != null) {
            cur.setStatus(order.getStatus());
        }

        if (order.getConfirmedBy() != null) {
            cur.setConfirmedBy(order.getConfirmedBy());
        }

        if (order.getPackagedBy() != null) {
            cur.setPackagedBy(order.getPackagedBy());
        }

        if (order.getShippedBy() != null) {
            cur.setShippedBy(order.getShippedBy());
        }

        return orderRepository.save(cur);
    }

    @Override
    public void delete(Long orderId) {
        orderRepository.delete(read(orderId));
    }

    @Override
    public void empConfirmOrder(Long orderId, Long empId) {
        Order order = read(orderId);

        order.setConfirmedBy(empId);

        orderRepository.save(order);
    }

    @Override
    public void empPackageOrder(Long orderId, Long empId) {
        Order order = read(orderId);

        order.setPackagedBy(empId);

        orderRepository.save(order);
    }

    @Override
    public void empShipOrder(Long orderId, Long empId) {
        Order order = read(orderId);

        order.setShippedBy(empId);

        orderRepository.save(order);
    }
}
