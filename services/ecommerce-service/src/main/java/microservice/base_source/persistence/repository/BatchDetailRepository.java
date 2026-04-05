package microservice.base_source.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.BatchDetail;

@Repository
public interface BatchDetailRepository extends JpaRepository<BatchDetail, String> {
	
}
