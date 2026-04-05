package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.SaleProduct;

public interface SaleProductUseCase {

	/**
	 * Lấy danh sách SaleProduct
	 * @return List SaleProduct
	 */
	List<SaleProduct> getAll(Integer page, Integer size);
	SaleProduct get(Long saleEventid, String batchId);
	SaleProduct get(String batchId);
	SaleProduct create(SaleProduct saleProduct);
	SaleProduct update(Long saleEventid, String batchId, SaleProduct saleProduct);
	void delete(Long saleEventid, String batchId);
}
