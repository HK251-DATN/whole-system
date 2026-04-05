package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.EnterpriseStore;

public interface EnterpriseStoreService {

    EnterpriseStore create(EnterpriseStore store);

    EnterpriseStore read(Long storeId);

    List<EnterpriseStore> readAll(Integer pageNum, Integer pageSize);

    EnterpriseStore update(Long storeId, EnterpriseStore store);

    void delete(Long storeId);
}
