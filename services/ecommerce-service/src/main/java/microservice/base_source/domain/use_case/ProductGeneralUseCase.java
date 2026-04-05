package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.ProductGeneral;

public interface ProductGeneralUseCase {

	/**
	 * Tìm kiếm productGeneral bằng name, description, categoryId
	 * @return danh sách productGeneral hỗ trợ pagination
	 */
	List<ProductGeneral> search(Long categoryId, String searchString, Integer page, Integer size);
	List<ProductGeneral> getAll(Integer page, Integer size);
	ProductGeneral get(Long id);
	ProductGeneral create(ProductGeneral productGeneral);
	ProductGeneral update(Long id, ProductGeneral productGeneral);
	void delete(Long id);
    ProductGeneral createFromEvent(ProductGeneral productGeneral);
}
