package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.ProductRequest;

public interface ProductRequestService {

    ProductRequest create(ProductRequest productRequest);

    ProductRequest read(Long prodReqId);

    List<ProductRequest> readAll(Integer pageNum, Integer pageSize);

    ProductRequest update(Long prodReqId, ProductRequest productRequest);

    void delete(Long prodReqId);
}
