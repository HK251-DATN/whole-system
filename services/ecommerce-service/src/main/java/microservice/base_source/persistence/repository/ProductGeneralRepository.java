package microservice.base_source.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Repository
public interface ProductGeneralRepository extends JpaRepository<ProductGeneral, Long> {
    
//    Logger log = LoggerFactory.getLogger(ProductGeneralRepository.class);
    
	@Query(
		value = """
            SELECT *
            FROM PRODUCT_GENERAL pg
            WHERE
                (:categoryId = 0 OR pg.category_id = :categoryId)
                AND (
                        :searchString = '' OR
                        LOWER(pg.name)        LIKE LOWER(CONCAT('%', :searchString, '%')) OR
                        LOWER(pg.description) LIKE LOWER(CONCAT('%', :searchString, '%'))
                    )
            LIMIT :size
            OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<ProductGeneral> search(
		@Param("categoryId") Long categoryId,
		@Param("searchString") String searchString,
		@Param("page") Integer page,
		@Param("size") Integer size
	);
    
    @Query(value = """
            SELECT
                PG.PRODUCT_GENERAL_ID 	AS "productGeneralId",
                PG.CATEGORY_ID 			AS "categoryId",
                PG.PROVIDER_ID 			AS "providerId",
                PG.NAME        			AS "name",
                PG.DESCRIPTION 			AS "description",
                   PG.IMG         			AS "img",
                   BD.BATCH_DETAIL_ID		AS "batchId",
                   BD.QUANTITY   			AS "quantity",
                   BD.PRICE      			AS "originPrice",
                   BD.AVG_RATE   			AS "avgRate",
                   BD.NUM_RATE   			AS "numRate",
                   BD.CREATED_AT 			AS "createdAt",
                COALESCE(SP.DIS_VAL, 0) AS "disVal",
                COALESCE(SP.SALE_EVENT_ID, 0) AS "saleEventId"
            FROM BATCH_DETAIL BD
            LEFT JOIN PRODUCT_GENERAL PG
                ON BD.PRODUCT_GENERAL_ID = PG.PRODUCT_GENERAL_ID
            LEFT JOIN SALE_PRODUCT SP
                ON SP.BATCH_ID = BD.BATCH_DETAIL_ID
            WHERE
                (:categoryId = 0 OR PG.CATEGORY_ID = :categoryId)
                AND (:productGeneralId = 0 OR BD.PRODUCT_GENERAL_ID = :productGeneralId)
                AND (
                    CAST(:searchString AS TEXT) = '' OR
                    LOWER(PG.NAME)        LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%')) OR
                    LOWER(PG.DESCRIPTION) LIKE LOWER(CONCAT('%', CAST(:searchString AS TEXT), '%'))
                )
                AND (:minPrice   = 0 OR BD.PRICE    >= :minPrice)
                AND (:maxPrice   = 0 OR BD.PRICE    <= :maxPrice)
                AND (:minRating  = 0 OR BD.AVG_RATE >= :minRating)
                AND (:maxRating  = 0 OR BD.AVG_RATE <= :maxRating)
                AND (:minNumRate = 0 OR BD.NUM_RATE >= :minNumRate)
                AND (:maxNumRate = 0 OR BD.NUM_RATE <= :maxNumRate)
                AND (
                    cast(:searchTags as text) IS NULL OR
                    cast(:searchTags as text) = '' OR
                    PG.TAGS && string_to_array(cast(:searchTags as text), ',')::text[]
                )
            ORDER BY
                CASE WHEN :createdSortOption = 'ASC'  THEN BD.CREATED_AT END ASC,
                CASE WHEN :createdSortOption = 'DESC' THEN BD.CREATED_AT END DESC,
                CASE WHEN :ratingSortOption  = 'ASC'  THEN BD.AVG_RATE   END ASC,
                CASE WHEN :ratingSortOption  = 'DESC' THEN BD.AVG_RATE   END DESC,
                CASE WHEN :numRateSortOption = 'ASC'  THEN BD.NUM_RATE   END ASC,
                CASE WHEN :numRateSortOption = 'DESC' THEN BD.NUM_RATE   END DESC
            LIMIT :size OFFSET (:page - 1) * :size;
			""", nativeQuery = true)
    List<DetailGeneralDTO> aggregatedSearch (
            @Param("categoryId") Long categoryId,
            @Param("productGeneralId") Long productGeneralId,
            @Param("searchString") String searchString,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            @Param("minNumRate") int minNumRate,
            @Param("maxNumRate") int maxNumRate,
            @Param("searchTags") String searchTags,
            @Param("createdSortOption") String createdSortOption,
            @Param("ratingSortOption") String ratingSortOption,
            @Param("numRateSortOption") String numRateSortOption,
            @Param("page") Integer page,
            @Param("size") Integer size
    );
}
