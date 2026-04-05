package microservice.base_source.domain.use_case;

import java.util.List;

import microservice.base_source.domain.entity.Buyer;

public interface BuyerUseCase {
	/**
	 * Tìm kiếm buyer theo name or aliasNm, email, phone
	 * @return List buyer
	 */
	List<Buyer> search(String searchName, String activeYn, Integer page, Integer size);

	/**
	 * Lấy danh sách buyer
	 * @return List buyer
	 */
	List<Buyer> getAll(Integer page, Integer size);
	Buyer get(String id);
	Buyer create(Buyer buyer);
	Buyer update(String id, Buyer buyer);
	void delete(String id);
}
