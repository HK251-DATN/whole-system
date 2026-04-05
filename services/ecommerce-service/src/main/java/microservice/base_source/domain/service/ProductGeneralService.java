package microservice.base_source.domain.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.domain.exception.type.ProductNotFoundException;
import microservice.base_source.domain.use_case.ProductGeneralUseCase;
import microservice.base_source.persistence.repository.ProductGeneralRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductGeneralService implements ProductGeneralUseCase {
	@Autowired
	private ProductGeneralRepository productGeneralRepository;

	@Override
	public List<ProductGeneral> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<ProductGeneral> productGeneralPage = productGeneralRepository.findAll(pageable);
		return productGeneralPage.getContent();
	}

	@Override
	public ProductGeneral get(Long id) {
		return productGeneralRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("ProductGeneral not found"));
	}

	@Override
	public ProductGeneral create(ProductGeneral productGeneral) {
//		ProductGeneral newProductGeneral = productGeneralRepository.save(productGeneral);
//
//		ProductGeneralCreatedEvent event = new ProductGeneralCreatedEvent(
//			newProductGeneral.getProductGeneralId(),
//			newProductGeneral.getName(),
//			newProductGeneral.getImg(),
//			newProductGeneral.getDescription(),
//			newProductGeneral.getCategoryId()
//		);
//
//		productGeneralProducer.publishProductGeneralCreated(event);
//
//		return newProductGeneral;
        return null;
	}

	@Override
	public ProductGeneral update(Long id, ProductGeneral productGeneral) {
		ProductGeneral existingProductGeneral = productGeneralRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("ProductGeneral not found"));

		// copy properties from productGeneral to existingProductGeneral
		BeanUtils.copyProperties(
				productGeneral,
				existingProductGeneral,
				"productGeneralId", "createdAt", "updatedAt", "deletedAt" // filed not update
		);
		return productGeneralRepository.save(existingProductGeneral);
	}

	@Override
	public void delete(Long id) {
		productGeneralRepository.findById(id)
			.ifPresentOrElse(
				productGeneralRepository::delete,
				() -> {}
			);
	}

	@Override
	public List<ProductGeneral> search(Long categoryId, String searchString, Integer page, Integer size) {
		return productGeneralRepository.search(categoryId, searchString, page, size);
	}
    
    @Override
    @Transactional
    public ProductGeneral createFromEvent(ProductGeneral productGeneral) {
        // Create product general from event with pre-assigned ID
        return productGeneralRepository.save(productGeneral);
    }
}
