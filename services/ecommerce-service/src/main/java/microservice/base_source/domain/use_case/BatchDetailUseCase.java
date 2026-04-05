package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.BatchDetail;

public interface BatchDetailUseCase {

	/**
	 * Lấy danh sách batchDetail
	 * @return List batchDetail
	 */
	List<BatchDetail> getAll(Integer page, Integer size);
	BatchDetail get(Long id);
	BatchDetail create(BatchDetail batchDetail);
	BatchDetail update(Long id, BatchDetail batchDetail);
	void delete(Long id);
}
