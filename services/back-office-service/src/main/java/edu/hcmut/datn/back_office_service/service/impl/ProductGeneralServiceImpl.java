package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;
import java.util.Objects;

import edu.hcmut.datn.back_office_service.dao.SubSubcategory;
import edu.hcmut.datn.back_office_service.messaging.productgeneral.ProductGeneralCreatedEvent;
import edu.hcmut.datn.back_office_service.messaging.productgeneral.ProductGeneralProducer;
import edu.hcmut.datn.back_office_service.repository.SubSubcategoryRepository;
import edu.hcmut.datn.back_office_service.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.ProductGeneral;
import edu.hcmut.datn.back_office_service.exception.productgeneral.ProductGeneralNotFoundException;
import edu.hcmut.datn.back_office_service.repository.ProductGeneralRepository;
import edu.hcmut.datn.back_office_service.service.ProductGeneralService;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductGeneralServiceImpl implements ProductGeneralService {

    private final ProductGeneralRepository productGeneralRepository;

    private final SubSubcategoryRepository subSubcategoryRepository;
    
    private final ProductGeneralProducer productGeneralProducer;
    
    private final R2UploadService r2UploadService;
    
    @Value("${app.product-general-img-bucket}")
    private String productGeneralImgBucket;
    
    @Value("${app.product-general-img-default-url}")
    private String productGeneralDefaultImgUrl;
    
    @Value("${app.product-general-image-public-bucket-url}")
    private String productGeneralImgPubUrlPrefix;
    
    @Override
    @Transactional
    public ProductGeneral create(ProductGeneral productGeneral) {
        // Save the product general
        ProductGeneral saved = productGeneralRepository.save(productGeneral);
        
        // Query to get the subcategory ID (parent of sub-subcategory)
        Long subcategoryId = null;
        if (saved.getSubSubcategoryId() != null) {
            SubSubcategory subSubcategory = subSubcategoryRepository
                    .findById(saved.getSubSubcategoryId())
                    .orElse(null);
            
            if (subSubcategory != null) {
                subcategoryId = subSubcategory.getSubcategoryId();
            }
        }
        
        // Publish event with both subSubcategoryId and derived categoryId
        ProductGeneralCreatedEvent event = new ProductGeneralCreatedEvent(
                saved.getProdGenId(),
                saved.getProdName(),
                productGeneralDefaultImgUrl,  // imgUrl
                saved.getDescription(),  // description
                saved.getUnit(),
                saved.getUnitQuantity(),
                saved.getSubSubcategoryId(),
                subcategoryId  // This is the subcategory ID for ecommerce
        );
        
        productGeneralProducer.publishProductGeneralCreated(event);
        log.info("Published ProductGeneralCreatedEvent for product: {}", saved.getProdGenId());
        
        return saved;
    }

    @Override
    public ProductGeneral read(Long productGeneralId) {
        return productGeneralRepository.findById(productGeneralId)
                .orElseThrow(() -> new ProductGeneralNotFoundException("Product General Not Found"));
    }

    @Override
    public List<ProductGeneral> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<ProductGeneral> page = productGeneralRepository.findAll(pageable);

        return page.toList();
    }

    @Override
    public ProductGeneral update(Long productGeneralId, ProductGeneral productGeneral) {
        ProductGeneral cur = read(productGeneralId);
        
        if (productGeneral.getProdName() != null) {
            cur.setProdName(productGeneral.getProdName());
        }
        
        if (productGeneral.getImgUrl() != null) {
            cur.setImgUrl(productGeneral.getImgUrl());
        }
        
        if (productGeneral.getDescription() != null) {
            cur.setDescription(productGeneral.getDescription());
        }
        
        if (productGeneral.getUnit() != null) {
            cur.setUnit(productGeneral.getUnit());
        }
        
        if (productGeneral.getUnitQuantity() != null) {
            cur.setUnitQuantity(productGeneral.getUnitQuantity());
        }
        
        if (productGeneral.getSubSubcategoryId() != null) {
            cur.setSubSubcategoryId(productGeneral.getSubSubcategoryId());
        }
        
        if (productGeneral.getPreorderPolicyId() != null) {
            cur.setPreorderPolicyId(productGeneral.getPreorderPolicyId());
        }
        
        if (productGeneral.getEnterpriseStoreId() != null) {
            cur.setEnterpriseStoreId(productGeneral.getEnterpriseStoreId());
        }
        
        if (productGeneral.getTags() != null) {
            cur.setTags(productGeneral.getTags());
        }

        return productGeneralRepository.save(cur);
    }

    @Override
    public void delete(Long productGeneralId) {
        productGeneralRepository.delete(read(productGeneralId));
    }
    
    @Override
    public ProductGeneral updateProductMainImage(Long productGeneralId, String imageUrl) {
        ProductGeneral curProductGeneral = read(productGeneralId);
        
        String oldImgUrl = curProductGeneral.getImgUrl();
        
        // Only delete old image if it's not the default one
        if (oldImgUrl != null && !Objects.equals(oldImgUrl, productGeneralDefaultImgUrl)) {
            String key = oldImgUrl.substring(productGeneralImgPubUrlPrefix.length() + 1);
            r2UploadService.delete(key, productGeneralImgBucket);
        }
        
        curProductGeneral.setImgUrl(imageUrl);
        
        return productGeneralRepository.save(curProductGeneral);
    }
}
