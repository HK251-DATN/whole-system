package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.CartUseCase;
import microservice.base_source.persistence.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart getByBuyerId(String buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for buyer: " + buyerId));
    }
}