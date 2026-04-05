package microservice.base_source.presentation.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.use_case.SearchUseCase;
import microservice.base_source.persistence.dto.DetailGeneralDTO;
import microservice.base_source.presentation.response.global.ApiResponse;
import microservice.base_source.presentation.response.searchProduct.ProductSearchResponse;

@RestController
@RequestMapping("/api/product-search")
@RequiredArgsConstructor
@Slf4j
public class ProductSearchController {
	@Autowired
	private SearchUseCase searchUseCase;


//		log.info(String.valueOf(categoryId));
//		log.info(String.valueOf(productGeneralId));
//		log.info(searchString);
//		log.info(String.valueOf(minPrice));
//		log.info(String.valueOf(maxPrice));
//		log.info(String.valueOf(minRating));
//		log.info(String.valueOf(maxRating));
//		log.info(String.valueOf(minNumRate));
//		log.info(String.valueOf(maxNumRate));
//		log.info(Arrays.toString(searchTags));
//		log.info(createdSortOption);
//		log.info(ratingSortOption);
//		log.info(numRateSortOption);
//		log.info(String.valueOf(page));
//		log.info(String.valueOf(size));
    
    
    @GetMapping
	public ApiResponse<List<ProductSearchResponse>> search(
		@RequestParam(defaultValue = "0", name = "categoryId") Long categoryId,
		@RequestParam(defaultValue = "0", name = "productGeneralId") Long productGeneralId,
		@RequestParam(defaultValue = "") String searchString,
		@RequestParam(defaultValue = "0") BigDecimal minPrice,
		@RequestParam(defaultValue = "0") BigDecimal maxPrice,
		@RequestParam(defaultValue = "0") BigDecimal minRating,
		@RequestParam(defaultValue = "0") BigDecimal maxRating,
		@RequestParam(defaultValue = "0") int minNumRate,
		@RequestParam(defaultValue = "0") int maxNumRate,
		@RequestParam(defaultValue = "") String searchTags,
		@RequestParam(defaultValue = "") String createdSortOption,
		@RequestParam(defaultValue = "") String ratingSortOption,
		@RequestParam(defaultValue = "") String numRateSortOption,
		@RequestParam(defaultValue = "1") Integer page,
		@RequestParam(defaultValue = "20") Integer size) {

		List<DetailGeneralDTO> results = searchUseCase.searchBatch(
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
		if (results.isEmpty()) {
			return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No products found", null);
		}

		// convert to ProductSearchResponse
		List<ProductSearchResponse> listResponse = new ArrayList<ProductSearchResponse>();
		results.forEach(
			dto -> {
				listResponse.add(ProductSearchResponse.toResponse(dto));
			}
		);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search products success", listResponse);
	}
}
