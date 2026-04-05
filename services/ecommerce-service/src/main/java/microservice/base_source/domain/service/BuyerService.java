package microservice.base_source.domain.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.Buyer;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.BuyerUseCase;
import microservice.base_source.persistence.repository.BuyerRepository;

import org.springframework.beans.BeanUtils;

@Service
public class BuyerService implements BuyerUseCase {
	@Autowired
	private BuyerRepository buyerRepository;

	@Override
	public List<Buyer> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Buyer> buyerPage = buyerRepository.findAll(pageable);
		return buyerPage.getContent();
	}

	@Override
	public Buyer get(String id) {
		return buyerRepository.findById(id).orElseThrow(() -> new NotFoundException("Buyer not found"));
	}

	@Override
	public Buyer create(Buyer category) {
		return buyerRepository.save(category);
	}

	@Override
	public Buyer update(String id, Buyer category) {
		Buyer existingCategory = buyerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Buyer not found"));
		// Update all fields 
		BeanUtils.copyProperties(category, existingCategory, "id");
		return buyerRepository.save(existingCategory);
	}

	@Override
	public void delete(String id) {
		buyerRepository.findById(id)
			.ifPresentOrElse(
				buyerRepository::delete,
				() -> {}	
			);
	}

	@Override
	public List<Buyer> search(String searchName, String activeYn, Integer page, Integer size) {
		return buyerRepository.search(searchName, activeYn, page, size);
	}
	
}
