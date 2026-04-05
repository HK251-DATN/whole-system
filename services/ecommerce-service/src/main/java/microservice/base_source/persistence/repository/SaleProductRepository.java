package microservice.base_source.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.SaleProduct;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {
	@Query(value = """
				SELECT EXISTS (
					SELECT 1
					FROM SALE_PRODUCT
					WHERE BATCH_ID = :batchId
					AND SALE_EVENT_ID = :saleEventId
				)
			""", nativeQuery = true)
	boolean existsByEventAndBatch(@Param("saleEventId") Long saleEventId, @Param("batchId") String batchId);

	@Query(value = """
				SELECT *
				FROM SALE_PRODUCT
				WHERE BATCH_ID = :batchId
					AND SALE_EVENT_ID = :saleEventId
				LIMIT 1
			""", nativeQuery = true)
	Optional<SaleProduct> findOneByEventAndBatch(@Param("saleEventId") Long saleEventId, @Param("batchId") String batchId);

	@Query(value = """
				SELECT *
				FROM SALE_PRODUCT
				WHERE BATCH_ID = :batchId
				LIMIT 1
			""", nativeQuery = true)
	Optional<SaleProduct> findOneByBatch(@Param("batchId") String batchId);
}