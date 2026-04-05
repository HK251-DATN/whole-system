package microservice.base_source.domain.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.FeedBack;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.FeedBackUseCase;
import microservice.base_source.persistence.dto.FeedBackDTO;
import microservice.base_source.persistence.repository.FeedBackRepository;

import org.springframework.beans.BeanUtils;

@Service
public class FeedBackService implements FeedBackUseCase {
	@Autowired
	private FeedBackRepository feedBackRepository;

	@Override
	public List<FeedBack> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<FeedBack> feedbackPage = feedBackRepository.findAll(pageable);
		return feedbackPage.getContent();
	}

	@Override
	public FeedBack get(Long id) {
		return feedBackRepository.findById(id).orElseThrow(() -> new NotFoundException("Feedback not found"));
	}

	@Override
	public List<FeedBackDTO> getByBatchId(Long batchId, Integer page, Integer size) {
		return feedBackRepository.getByBatchId(batchId, page, size);
	}

	@Override
	public FeedBack create(FeedBack feedback) {
		return feedBackRepository.save(feedback);
	}

	@Override
	public FeedBack update(Long id, FeedBack feedback) {
		FeedBack existingfeedback = feedBackRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Feedback not found"));
		// Update all fields 
		BeanUtils.copyProperties(feedback, existingfeedback, "id");
		return feedBackRepository.save(existingfeedback);
	}

	@Override
	public void delete(Long id) {
		feedBackRepository.findById(id)
			.ifPresentOrElse(
				feedBackRepository::delete,
				() -> {}	
			);
	}
}