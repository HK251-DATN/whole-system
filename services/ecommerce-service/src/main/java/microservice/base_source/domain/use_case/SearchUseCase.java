package microservice.base_source.domain.use_case;

import java.math.BigDecimal;
import java.util.List;

import microservice.base_source.persistence.dto.DetailGeneralDTO;

public interface SearchUseCase {

	/**
	 * search distinct general - search bar
	 * @param tags array tag name
	 * @return list DetailGeneralDTO
	 */
	// List<DetailGeneralDTO> searchGeneral(
	// 	String searchString,
	// 	BigDecimal minPrice,
	// 	BigDecimal maxPrice,
	// 	BigDecimal minRating,
	// 	BigDecimal maxRating,
	// 	String[] tags,
	// 	String createdSortOption, // ASC/DESC
	// 	String ratingSortOption, // ASC/DESC
	// 	Integer page,
	// 	Integer size
	// ); 

	/**
	 * search distict batch after search general WHEN buyer vieww all product
	 * @return list DetailGeneralDTO
	 */
	List<DetailGeneralDTO> searchBatch(
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
		Integer size
	);
}
