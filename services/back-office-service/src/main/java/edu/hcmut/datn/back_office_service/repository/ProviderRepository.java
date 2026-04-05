package edu.hcmut.datn.back_office_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.back_office_service.dao.Provider;

public interface ProviderRepository extends JpaRepository<Provider, Long>{

    Boolean existsByUserId(Long userId);
}
