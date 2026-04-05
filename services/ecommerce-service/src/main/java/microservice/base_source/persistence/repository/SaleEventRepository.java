package microservice.base_source.persistence.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.SaleEvent;

@Repository
public interface SaleEventRepository extends JpaRepository<SaleEvent, Long> {

	@Query(value = """
			SELECT * FROM SALE_EVENT SE
			WHERE (:searchString = '' OR 
				SE.NAME 	   LIKE CONCAT('%', :searchString, '%') OR 
				SE.DESCRIPTION LIKE CONCAT('%', :searchString, '%'))
				AND (:activeYn  = 'Y' 	OR SE.ACTIVE_YN  = :activeYn)
				AND (:enableYn  = 'Y' 	OR SE.ENABLED_YN = :enableYn)
				AND (:beginTime IS NULL OR SE.BEGIN_TIME >= :beginTime)
				AND (:endTime 	IS NULL OR SE.END_TIME 	 <= :endTime)
				AND (:beginDate IS NULL OR SE.BEGIN_DATE >= :beginDate)
				AND (:endDate 	IS NULL OR SE.END_DATE 	 <= :endDate)
			LIMIT :size 
			OFFSET (:page - 1) * :size;
			""",
			nativeQuery = true)
	List<SaleEvent> search(
		@Param("searchString") String searchString, 
		@Param("activeYn")  String activeYn, 
		@Param("enableYn")  String enableYn, 
		@Param("beginTime") LocalTime beginTime,
		@Param("endTime") 	LocalTime endTime, 
		@Param("beginDate") LocalDateTime beginDate, 
		@Param("endDate") 	LocalDateTime endDate,
		@Param("page") int page,
		@Param("size") int size
	);
}