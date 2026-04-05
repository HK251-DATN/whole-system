package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.ProductGeneral;

public interface ProductGeneralService {

    ProductGeneral create(ProductGeneral productGeneral);

    ProductGeneral read(Long productGeneralId);

    List<ProductGeneral> readAll(Integer pageNum, Integer pageSize);

    ProductGeneral update(Long productGeneralId, ProductGeneral productGeneral);

    void delete(Long productGeneralId);

    ProductGeneral updateProductMainImage(Long productGeneralId, String imageUrl);
}
