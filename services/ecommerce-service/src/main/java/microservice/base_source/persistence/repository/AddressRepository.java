package microservice.base_source.persistence.repository;

import microservice.base_source.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByBuyerId(String buyerId);
}
