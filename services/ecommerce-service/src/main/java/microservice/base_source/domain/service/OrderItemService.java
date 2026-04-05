package microservice.base_source.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.OrderItem;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.exception.type.WarnException;
import microservice.base_source.domain.use_case.OrderItemUseCase;
import microservice.base_source.persistence.repository.OrderItemRepository;

@Service
public class OrderItemService implements OrderItemUseCase {

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Override
	public List<OrderItem> getAll(Long buyerId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<OrderItem> orderItemList = orderItemRepository.findAll(pageable);
		return orderItemList.getContent();
	}

	@Override
	public OrderItem get(Long id) {
		return orderItemRepository.findById(id).orElseThrow(() -> new NotFoundException("OrderItem not found"));
	}

	@Override
	public OrderItem create(OrderItem orderItem) {
		return orderItemRepository.save(orderItem);
	}

	@Override
	public OrderItem update(Long id, OrderItem orderItem) {
//		OrderItem existingOrderItem = orderItemRepository.findById(id)
//				.orElseThrow(() -> new NotFoundException("OrderItem not found"));
//		if (existingOrderItem.getTempYn().equals("N")) {
//			throw new WarnException("Đơn hàng đã tạo thành công");
//		}
//		// Update productDetailId, quantity, originalPrice, salePrice, tempYn
//		existingOrderItem.setProductDetailId(orderItem.getProductDetailId());
//		existingOrderItem.setQuantity(orderItem.getQuantity());
//		existingOrderItem.setOriginalPrice(orderItem.getOriginalPrice());
//		existingOrderItem.setSalePrice(orderItem.getSalePrice());
//		existingOrderItem.setTempYn(orderItem.getTempYn());
		return null;
	}

	@Override
	public void delete(Long id) {
		orderItemRepository.findById(id)
			.ifPresentOrElse(
				orderItemRepository::delete,
				() -> {}	
			);
	}
}
