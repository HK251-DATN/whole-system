package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.FeedBack;
import microservice.base_source.persistence.dto.FeedBackDTO;

public interface FeedBackUseCase {
	/**
	 * Get list FeedBack by batchId
	 * @return List FeedBackDTO
	 */
	List<FeedBackDTO> getByBatchId(Long batchId, Integer page, Integer size);

	/**
	 * Lấy danh sách FeedBack
	 * @return List FeedBack
	 */
	List<FeedBack> getAll(Integer page, Integer size);
	FeedBack get(Long id);
	FeedBack create(FeedBack feedback);
	FeedBack update(Long id, FeedBack feedback);
	void delete(Long id);
}