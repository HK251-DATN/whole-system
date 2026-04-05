package microservice.base_source.persistence.repository;

import microservice.base_source.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByBuyerId(String buyerId);
    boolean existsByBuyerId(String buyerId);
}
