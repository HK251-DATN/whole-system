package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.use_case.SearchUseCase;
import microservice.base_source.persistence.dto.DetailGeneralDTO;
import microservice.base_source.persistence.repository.ProductGeneralRepository;

@Service
public class SearchService implements SearchUseCase {

	@Autowired
	private ProductGeneralRepository productGeneralRepository;

	// @Override
	// public List<DetailGeneralDTO> searchGeneral(String searchString, BigDecimal minPrice, BigDecimal maxPrice,
	// 		BigDecimal minRating, BigDecimal maxRating, String[] tags, String createdSortOption,
	// 		String ratingSortOption, Integer page, Integer size) {
	// 	throw new UnsupportedOperationException("Unimplemented method 'searchGeneral'");
	// }

	@Override
	public List<DetailGeneralDTO> searchBatch(
            Long categoryId,
            Long productGeneralId,
            String searchString,
			BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating,
            BigDecimal maxRating,
            int minNumRate,
			int maxNumRate,
            String searchTags,
            String createdSortOption,
            String ratingSortOption,
			String numRateSortOption,
            Integer page,
            Integer size) {
		return productGeneralRepository.aggregatedSearch(
			categoryId,
			productGeneralId,
			searchString,
			minPrice,
			maxPrice,
			minRating,
			maxRating,
			minNumRate,
			maxNumRate,
			searchTags,
			createdSortOption,
			ratingSortOption,
			numRateSortOption,
			page,
			size
		);
	}
}
