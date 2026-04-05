package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.ProductDetail;

public interface ProductDetailUseCase {
	List<ProductDetail> getAll(Integer page, Integer size);
	ProductDetail get(Long id);
	ProductDetail create(ProductDetail productDetail);
	ProductDetail update(Long id, ProductDetail productDetail);
	void delete(Long id);
}
