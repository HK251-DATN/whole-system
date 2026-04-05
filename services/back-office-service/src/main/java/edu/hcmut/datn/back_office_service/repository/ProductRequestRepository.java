package edu.hcmut.datn.back_office_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.back_office_service.dao.ProductRequest;

public interface ProductRequestRepository extends JpaRepository<ProductRequest, Long> {

}
