package microservice.base_source.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.Order;
import microservice.base_source.domain.entity.Order.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by buyer ID with pagination
    Page<Order> findByBuyerId(String buyerId, Pageable pageable);
    
    // Find orders by buyer ID and status
    List<Order> findByBuyerIdAndStatus(String buyerId, OrderStatus status);
    
	@Query(value = """
			SELECT * FROM ORDERS ORD
			WHERE (:buyerId = '' 		OR ORD.BUYER_ID 	= :buyerId)
				AND (:searchString = '' OR ORD.NOTE LIKE CONCAT('%', :searchString, '%'))
				AND (:status   IS NULL  OR ORD.STATUS 		= :status)
				AND (:minPrice IS NULL  OR ORD.TOTAL_PRICE >= :minPrice)
				AND (:maxPrice IS NULL  OR ORD.TOTAL_PRICE <= :maxPrice)
				AND (:minTime  IS NULL  OR ORD.CREATED_AT  >= :minTime)
				AND (:maxTime  IS NULL  OR ORD.CREATED_AT  <= :maxTime)
			ORDER BY
				CASE WHEN :sortByStatus = 'ASC'  THEN ORD.STATUS 	  END ASC,
				CASE WHEN :sortByStatus = 'DESC' THEN ORD.STATUS 	  END DESC,
				CASE WHEN :sortByPrice  = 'ASC'  THEN ORD.TOTAL_PRICE END ASC,
				CASE WHEN :sortByPrice  = 'DESC' THEN ORD.TOTAL_PRICE END DESC,
				CASE WHEN :sortByTime   = 'ASC'  THEN ORD.CREATED_AT  END ASC,
				CASE WHEN :sortByTime   = 'DESC' THEN ORD.CREATED_AT  END DESC
			LIMIT :size 
			OFFSET (:page - 1) * :size;
			""",
			nativeQuery = true)
	List<Order> search(
		@Param("buyerId") String buyerId, 
		@Param("searchString") String searchString, 
		@Param("status") OrderStatus status, 
		@Param("minPrice") BigDecimal minPrice,
		@Param("maxPrice") BigDecimal maxPrice, 
		@Param("minTime") LocalDateTime minTime, 
		@Param("maxTime") LocalDateTime maxTime, 
		@Param("sortByStatus") String sortByStatus, 
		@Param("sortByPrice") String sortByPrice,
		@Param("sortByTime") String sortByTime, 
		@Param("page") int page, 
		@Param("size") int size
	);
}
