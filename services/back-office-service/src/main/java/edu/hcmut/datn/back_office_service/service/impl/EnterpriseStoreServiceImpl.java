package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.EnterpriseStore;
import edu.hcmut.datn.back_office_service.exception.enterprisestore.EnterpriseStoreAlreadyExistsException;
import edu.hcmut.datn.back_office_service.exception.enterprisestore.EnterpriseStoreNotFoundException;
import edu.hcmut.datn.back_office_service.repository.EnterpriseStoreRepository;
import edu.hcmut.datn.back_office_service.service.EnterpriseStoreService;

@Service
public class EnterpriseStoreServiceImpl implements EnterpriseStoreService {

    private final EnterpriseStoreRepository enterpriseStoreRepository;

    public EnterpriseStoreServiceImpl(EnterpriseStoreRepository enterpriseStoreRepository) {
        this.enterpriseStoreRepository = enterpriseStoreRepository;
    }

    @Override
    public EnterpriseStore create(EnterpriseStore store) {
        if (Boolean.TRUE.equals(enterpriseStoreRepository.existsByProviderId(store.getProviderId()))) {
            throw new EnterpriseStoreAlreadyExistsException("This provider has already linked to a exist store!");
        }

        return enterpriseStoreRepository.save(store);
    }

    @Override
    public EnterpriseStore read(Long storeId) {
        return enterpriseStoreRepository.findById(storeId).orElseThrow(
            () -> new EnterpriseStoreNotFoundException("Enterprise store not found")
        );
    }

    @Override
    public List<EnterpriseStore> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		return enterpriseStoreRepository.findAll(pageable).toList();
    }

    @Override
    public EnterpriseStore update(Long storeId, EnterpriseStore store) {
        EnterpriseStore curStore = read(storeId);

        if (store.getStoreName() != null) {
            curStore.setStoreName(store.getStoreName());
        }

        if (store.getStoreDes() != null) {
            curStore.setStoreDes(store.getStoreDes());
        }

        return enterpriseStoreRepository.save(curStore);
    }

    @Override
    public void delete(Long storeId) {
        EnterpriseStore store = read(storeId);

        enterpriseStoreRepository.delete(store);
    }
}
