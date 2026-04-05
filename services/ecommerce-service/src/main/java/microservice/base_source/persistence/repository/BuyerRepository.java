package microservice.base_source.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.Buyer;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, String> {
	@Query(
		value = """
            SELECT *
            FROM BUYER B
            WHERE 
				B.activeYn = :activeYn
				AND (
					:searchString = '' OR
					LOWER(B.NAME) 	 LIKE LOWER(CONCAT('%', :searchString, '%')) OR 
					LOWER(B.ALIASNM) LIKE LOWER(CONCAT('%', :searchString, '%')) OR 
					LOWER(B.PHONE) 	 LIKE LOWER(CONCAT('%', :searchString, '%')) OR 
					LOWER(B.EMAIL) 	 LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
            LIMIT :size 
			OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<Buyer> search(
		@Param("searchString") String searchString,
		@Param("activeYn") String activeYn,
		@Param("page") Integer page, 
		@Param("size") Integer size
	);
}
