package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import edu.hcmut.datn.back_office_service.exception.productrequest.ProductRequestNotFoundException;
import edu.hcmut.datn.back_office_service.repository.ProductRequestRepository;
import edu.hcmut.datn.back_office_service.service.ProductRequestService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProductRequestServiceImpl implements ProductRequestService {

    private final ProductRequestRepository repository;

    @Override
    public ProductRequest create(ProductRequest productRequest) {
        return repository.save(productRequest);
    }

    @Override
    public ProductRequest read(Long prodReqId) {
        return repository.findById(prodReqId)
                .orElseThrow(() -> new ProductRequestNotFoundException("Product request not found"));
    }

    @Override
    public List<ProductRequest> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<ProductRequest> page = repository.findAll(pageable);

        return page.toList();
    }

    @Override
    public ProductRequest update(Long prodReqId, ProductRequest productRequest) {
        ProductRequest curRequest = read(prodReqId);

        if (productRequest.getQuantity() != null) {
            curRequest.setQuantity(productRequest.getQuantity());
        }

        if (productRequest.getRequiredAfterDays() != null) {
            curRequest.setRequiredAfterDays(productRequest.getRequiredAfterDays());
        }

        return repository.save(curRequest);
    }

    @Override
    public void delete(Long prodReqId) {
        ProductRequest curRequest = read(prodReqId);

        repository.delete(curRequest);
    }

}
