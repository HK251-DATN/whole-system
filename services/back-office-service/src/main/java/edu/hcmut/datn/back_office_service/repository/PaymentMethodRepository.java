package edu.hcmut.datn.back_office_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.back_office_service.dao.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByBuyerId(Long buyerId);
}
