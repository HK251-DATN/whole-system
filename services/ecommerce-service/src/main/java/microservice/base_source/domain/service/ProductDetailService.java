package microservice.base_source.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.ProductDetail;
import microservice.base_source.domain.exception.type.ProductNotFoundException;
import microservice.base_source.domain.use_case.ProductDetailUseCase;
import microservice.base_source.persistence.repository.ProductDetailRepository;

@Service
public class ProductDetailService implements ProductDetailUseCase {
	@Autowired
	private ProductDetailRepository productDetailRepository;

	@Override
	public List<ProductDetail> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<ProductDetail> productDetailPage = productDetailRepository.findAll(pageable);
		return productDetailPage.getContent();
	}

	@Override
	public ProductDetail get(Long id) {
		return productDetailRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("ProductDetail not found"));
	}

	@Override
	public ProductDetail create(ProductDetail productDetail) {
		return productDetailRepository.save(productDetail);
	}

	@Override
	public ProductDetail update(Long id, ProductDetail productDetail) {
		ProductDetail existingProductDetail = productDetailRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("ProductDetail not found"));

		return productDetailRepository.save(existingProductDetail);
	}

	@Override
	public void delete(Long id) {
		productDetailRepository.findById(id)
			.ifPresentOrElse(
				productDetailRepository::delete,
				() -> {}
			);
	}
}
