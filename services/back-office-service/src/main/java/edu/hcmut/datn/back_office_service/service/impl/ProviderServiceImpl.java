package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Provider;
import edu.hcmut.datn.back_office_service.exception.provider.ProviderAlreadyExistsException;
import edu.hcmut.datn.back_office_service.exception.provider.ProviderNotFoundException;
import edu.hcmut.datn.back_office_service.repository.ProviderRepository;
import edu.hcmut.datn.back_office_service.service.ProviderService;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderServiceImpl(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public Provider create(Provider provider) {
        if (Boolean.TRUE.equals(providerRepository.existsByUserId(provider.getUserId()))) {
            throw new ProviderAlreadyExistsException("This user id has already linked with one provider account");
        }

        return providerRepository.save(provider);
    }

    @Override
    public Provider read(Long providerId) {
        return providerRepository.findById(providerId).orElseThrow(() -> new ProviderNotFoundException("Provider not found"));
    }

    @Override
    public List<Provider> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return providerRepository.findAll(pageable).toList();
    }

    @Override
    public Provider update(Long providerId, Provider provider) {
        Provider curProvider = read(providerId);

        if (provider.getReputationPoint() != null) {
            curProvider.setReputationPoint(provider.getReputationPoint());
        }

        if (provider.getVerificationStatus() != null) {
            curProvider.setVerificationStatus(provider.getVerificationStatus());
        }

        if (provider.getBankId() != null) {
            curProvider.setBankId(provider.getBankId());
        }

        if (provider.getBankNum() != null) {
            curProvider.setBankNum(provider.getBankNum());
        }

        return providerRepository.save(curProvider);
    }

    @Override
    public void delete(Long providerId) {
        Provider curProvider = read(providerId);

        providerRepository.delete(curProvider);
    }
}
