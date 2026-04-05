package microservice.base_source.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.ProductDetail;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

	@Query(value = """
		SELECT 
			PD.PRODUCT_DETAIL_ID AS productDetailId,
			PD.DESCRIPTION AS description,
			PD.STATUS AS status,
			PD.QUANTITY_AVAILABLE AS quantityAvailable,
			PD.PRICE AS price,
			PD.RATING AS rating,
			PD.CREATED_AT AS createdAt,
			PD.UPDATED_AT AS updatedAt,
			PG.PRODUCT_GENERAL_ID AS productGeneralId,
			PG.CATEGORY_ID AS categoryId,
			PG.PRODUCT_NAME AS productName,
			PG.DESCRIPTION AS generalDescription,
			PG.STATUS AS generalStatus,
			PG.PHOTO_URLS AS photoUrls
		FROM PRODUCT_DETAIL PD
		JOIN PRODUCT_GENERAL PG
			ON PD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
		WHERE 
			(:categoryId = 0 OR PG.CATEGORY_ID = :categoryId)
			AND (
				:searchString = '' OR
				LOWER(PG.PRODUCT_NAME) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
				LOWER(PD.DESCRIPTION)  LIKE LOWER(CONCAT('%', :searchString, '%')) OR
				LOWER(PG.DESCRIPTION)  LIKE LOWER(CONCAT('%', :searchString, '%'))
			)
		LIMIT :size OFFSET (:page - 1) * :size
	""", nativeQuery = true)
	List<DetailGeneralDTO> search(
		@Param("categoryId") Long categoryId, 
		@Param("searchString") String searchString, 
		@Param("page") Integer page,
		@Param("size") Integer size);

	@Query(value = """
			SELECT
				PD.PRODUCT_DETAIL_ID AS productDetailId,
				PD.DESCRIPTION AS description,
				PD.STATUS AS status,
				PD.QUANTITY_AVAILABLE AS quantityAvailable,
				PD.PRICE AS price,
				PD.RATING AS rating,
				PD.CREATED_AT AS createdAt,
				PD.UPDATED_AT AS updatedAt,
				PG.PRODUCT_GENERAL_ID AS productGeneralId,
				PG.CATEGORY_ID AS categoryId,
				PG.PRODUCT_NAME AS productName,
				PG.DESCRIPTION AS generalDescription,
				PG.STATUS AS generalStatus,
				PG.PHOTO_URLS AS photoUrls
			FROM PRODUCT_DETAIL PD
			JOIN PRODUCT_GENERAL PG
				ON PD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
			WHERE
				(:categoryId = 0 OR PG.CATEGORY_ID = :categoryId)
				AND (:productGeneralId = 0 OR PG.PRODUCT_GENERAL_ID = :productGeneralId)
				AND (
					:searchString = '' OR
					LOWER(PG.PRODUCT_NAME) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
					LOWER(PG.DESCRIPTION) LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
				AND (:minPrice  = 0 OR PD.PRICE  >= :minPrice)
				AND (:maxPrice  = 0 OR PD.PRICE  <= :maxPrice)
				AND (:minRating = 0 OR PD.RATING >= :minRating)
				AND (:maxRating = 0 OR PD.RATING <= :maxRating)
				AND (
					:searchTags IS NULL OR
					cardinality(:searchTags) = 0 OR
					PG.TAGS && CAST(:searchTags AS text[])
				)
			ORDER BY
				CASE WHEN :createdSortOption = 'ASC'  THEN PD.CREATED_AT END ASC,
				CASE WHEN :createdSortOption = 'DESC' THEN PD.CREATED_AT END DESC,
				CASE WHEN :ratingSortOption  = 'ASC'  THEN PD.RATING     END ASC,
				CASE WHEN :ratingSortOption  = 'DESC' THEN PD.RATING     END DESC
			LIMIT :size OFFSET (:page - 1) * :size
			""", nativeQuery = true)
	List<DetailGeneralDTO> aggregatedSearch(
		@Param("categoryId") Long categoryId,
		@Param("productGeneralId") Long productGeneralId,
		@Param("searchString") String searchString,
		@Param("minPrice") BigDecimal minPrice,
		@Param("maxPrice") BigDecimal maxPrice,
		@Param("minRating") BigDecimal minRating,
		@Param("maxRating") BigDecimal maxRating,
		@Param("searchTags") String[] searchTags,
		@Param("createdSortOption") String createdSortOption, // ASC/DESC
		@Param("ratingSortOption") String ratingSortOption,
		@Param("page") Integer page,
		@Param("size") Integer size
	);

	@Query(value = """
			SELECT
				COUNT(*)
			FROM PRODUCT_DETAIL PD
			JOIN PRODUCT_GENERAL PG
				ON PD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
			WHERE
				(:categoryId = 0 OR PG.CATEGORY_ID = :categoryId)
				AND (:productGeneralId = 0 OR PG.PRODUCT_GENERAL_ID = :productGeneralId)
				AND (
					:searchString = '' OR
					LOWER(PG.PRODUCT_NAME) LIKE LOWER(CONCAT('%', :searchString, '%')) OR
					LOWER(PG.DESCRIPTION)  LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
				AND (:minPrice  = 0 OR PD.PRICE  >= :minPrice)
				AND (:maxPrice  = 0 OR PD.PRICE  <= :maxPrice)
				AND (:minRating = 0 OR PD.RATING >= :minRating)
				AND (:maxRating = 0 OR PD.RATING <= :maxRating)
				AND (
					:searchTags IS NULL OR
					cardinality(:searchTags) = 0 OR
					PG.TAGS && CAST(:searchTags AS text[])
				)
			""", nativeQuery = true)
	Long countAggregatedSearch(
		@Param("categoryId") Long categoryId,
		@Param("productGeneralId") Long productGeneralId,
		@Param("searchString") String searchString,
		@Param("minPrice") BigDecimal minPrice,
		@Param("maxPrice") BigDecimal maxPrice,
		@Param("minRating") BigDecimal minRating,
		@Param("maxRating") BigDecimal maxRating,
		@Param("searchTags") String[] searchTags
	);
}
