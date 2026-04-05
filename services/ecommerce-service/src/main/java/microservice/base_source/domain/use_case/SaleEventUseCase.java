package microservice.base_source.domain.use_case;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import microservice.base_source.domain.entity.SaleEvent;

public interface SaleEventUseCase {

	/**
	 * Search SaleEvent by searchString 
	 * <p> Filter: activeYn, enableYn, beginTime, endTime, beginDate, endDate
	 * <p> Sort: not sort to manage a few event
	 * <p> Pass null to skip: beginTime, endTime, beginDate, endDate
	 * @return List SaleEvent
	 */
	List<SaleEvent> searchEvents(String searchString, 
		String activeYn, String enableYn,
		LocalTime beginTime, LocalTime endTime,
		LocalDateTime beginDate, LocalDateTime endDate,
		int page, int size
	);

	/**
	 * Lấy danh sách SaleEvent
	 * @return List SaleEvent
	 */
	List<SaleEvent> getAll(Integer page, Integer size);
	SaleEvent get(Long saleEventid);
	SaleEvent create(SaleEvent saleEvent);
	SaleEvent update(Long saleEventid, SaleEvent saleEvent);
	void delete(Long saleEventid);
}
