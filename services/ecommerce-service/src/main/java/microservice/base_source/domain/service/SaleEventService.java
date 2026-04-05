package microservice.base_source.domain.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.SaleEvent;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.SaleEventUseCase;
import microservice.base_source.persistence.repository.SaleEventRepository;

@Service
public class SaleEventService implements SaleEventUseCase {
	@Autowired
	private SaleEventRepository saleEventRepository;

	@Override
	public List<SaleEvent> searchEvents(String searchString, String activeYn, String enableYn, LocalTime beginTime,
			LocalTime endTime, LocalDateTime beginDate, LocalDateTime endDate, int page, int size) {
		return saleEventRepository.search(searchString, activeYn, enableYn, beginTime, endTime, beginDate, endDate, page, size);
	}

	@Override
	public List<SaleEvent> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<SaleEvent> saleEventPage = saleEventRepository.findAll(pageable);
		return saleEventPage.getContent();
	}

	@Override
	public SaleEvent get(Long saleEventid) {
		return saleEventRepository.findById(saleEventid).orElseThrow(() -> new NotFoundException("SaleEvent not found"));
	}

	@Override
	public SaleEvent create(SaleEvent saleEvent) {
		return saleEventRepository.save(saleEvent);
	}

	@Override
	public SaleEvent update(Long saleEventid, SaleEvent saleEvent) {
		SaleEvent existingSaleEvent = saleEventRepository.findById(saleEventid)
				.orElseThrow(() -> new NotFoundException("SaleEvent not found"));

		// Update all fields
		existingSaleEvent.setName(saleEvent.getName());
		existingSaleEvent.setDescription(saleEvent.getDescription());
		existingSaleEvent.setImg(saleEvent.getImg());
		existingSaleEvent.setDisplayPriority(saleEvent.getDisplayPriority());
		existingSaleEvent.setActiveYn(saleEvent.getActiveYn());
		existingSaleEvent.setEnabledYn(saleEvent.getEnabledYn());
		existingSaleEvent.setBeginTime(saleEvent.getBeginTime());
		existingSaleEvent.setEndTime(saleEvent.getEndTime());
		existingSaleEvent.setBeginDate(saleEvent.getBeginDate());
		existingSaleEvent.setEndDate(saleEvent.getEndDate());  
		
		return saleEventRepository.save(existingSaleEvent);
	}

	@Override
	public void delete(Long saleEventid) {
		saleEventRepository.findById(saleEventid)
			.ifPresentOrElse(
				saleEventRepository::delete,
				() -> {}
			);
	}
	
}
