package microservice.base_source.domain.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.BatchDetail;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.BatchDetailUseCase;
import microservice.base_source.persistence.repository.BatchDetailRepository;

import org.springframework.beans.BeanUtils;

@Service
public class BatchDetailService implements BatchDetailUseCase {
	@Autowired
	private BatchDetailRepository batchDetailRepository;

	@Override
	public List<BatchDetail> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<BatchDetail> batchDetailPage = batchDetailRepository.findAll(pageable);
		return batchDetailPage.getContent();
	}

	@Override
	public BatchDetail get(Long id) {
		return batchDetailRepository.findById(id.toString()).orElseThrow(() -> new NotFoundException("BatchDetail not found"));
	}

	@Override
	public BatchDetail create(BatchDetail batchDetail) {
		return batchDetailRepository.save(batchDetail);
	}

	@Override
	public BatchDetail update(Long id, BatchDetail batchDetail) {
		BatchDetail existingCategory = batchDetailRepository.findById(id.toString())
				.orElseThrow(() -> new NotFoundException("BatchDetail not found"));
		// Update all fields 
		BeanUtils.copyProperties(batchDetail, existingCategory, "id");
		return batchDetailRepository.save(existingCategory);
	}

	@Override
	public void delete(Long id) {
		batchDetailRepository.findById(id.toString())
			.ifPresentOrElse(
				batchDetailRepository::delete,
				() -> {}	
			);
	}
}
