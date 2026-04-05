package microservice.base_source.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import microservice.base_source.domain.entity.*;
import microservice.base_source.domain.exception.type.BadRequestException;
import microservice.base_source.domain.exception.type.UnauthorizedException;
import microservice.base_source.infrastructure.messaging.order.OrderCreatedEvent;
import microservice.base_source.infrastructure.messaging.order.OrderProducer;
import microservice.base_source.infrastructure.messaging.orderitem.OrderItemCreatedEvent;
import microservice.base_source.infrastructure.messaging.orderitem.OrderItemProducer;
import microservice.base_source.persistence.repository.*;
import microservice.base_source.presentation.response.order.OrderDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import microservice.base_source.domain.entity.Order.OrderStatus;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.OrderUseCase;

@Service
public class OrderService implements OrderUseCase {
	@Autowired
	private OrderRepository orderRepository;

	@PersistenceContext
    EntityManager entityManager;

	@Autowired
	private OrderProducer orderProducer;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CartRepository cartRepository;
	@Autowired
	private CartItemRepository cartItemRepository;
	@Autowired
	private BatchDetailRepository batchDetailRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderItemProducer orderItemProducer;


	@Override
	public List<Order> search(String buyerId, String searchString, OrderStatus status, BigDecimal minPrice,
			BigDecimal maxPrice, LocalDateTime minTime, LocalDateTime maxTime, String sortByStatus, String sortByPrice,
			String sortByTime, int page, int size) {
		return orderRepository.search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime,
				sortByStatus, sortByPrice, sortByTime, page, size);
	}

	@Override
	@Transactional(
		rollbackFor = Exception.class,
		propagation = Propagation.REQUIRED
	)
	public Order create(Order order, List<OrderItem> orderItems) {

//
//		// check product in stocks
//
//		// insert order
//		Order insertedOrder = orderRepository.save(order);
//
//		// insert order item
//		orderItems.forEach(orderItem -> {
//			orderItem.setOrderId(insertedOrder.getOrderId());
//		});
//		saveBatch(orderItems);
//
//		orderProducer.publishOrderCreated(new OrderCreatedEvent(insertedOrder.getOrderId()));
//
//		// update quantity batch detail & status product detail
//		return insertedOrder;

		return null;
	}

	@Override
	public List<Order> getAll(String buyerId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Order> orderPage = orderRepository.findAll(pageable);
		return orderPage.getContent();
	}

	@Override
	public Order get(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
	}
    
    @Override
    public List<Order> getByBuyerId(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        return orderPage.getContent();
    }

	// @Override
	// public Order update(Long id, Order order) {
	// 	// not support update order
	// 	throw new UnsupportedOperationException("Unimplemented method 'update'");
	// }

	@Override
	public void delete(Long id) {
		orderRepository.findById(id)
			.ifPresentOrElse(
				orderRepository::delete,
				() -> {}	
			);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void saveBatch(List<OrderItem> items) {
		int batchSize = 50;

		for (int i = 0; i < items.size(); i++) {
			entityManager.persist(items.get(i));

			if (i > 0 && i % batchSize == 0) {
				entityManager.flush();
				entityManager.clear();
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public Order createFromCart(String buyerId, Long addressId) {
		// 1. Validate address belongs to user
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new NotFoundException("Address not found"));

		if (!address.getBuyerId().equals(buyerId)) {
			throw new UnauthorizedException("Address does not belong to the user");
		}

		// 2. Find user's cart
		Cart cart = cartRepository.findByBuyerId(buyerId)
				.orElseThrow(() -> new NotFoundException("Cart not found for user"));

		// 3. Get selected cart items
		List<CartItem> selectedItems = cartItemRepository
				.findByCartIdAndIsSelected(cart.getCartId(), true);

		if (selectedItems.isEmpty()) {
			throw new BadRequestException("No items selected in cart");
		}

		// 4. Validate stock and calculate total
		BigDecimal totalPrice = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : selectedItems) {
			// Fetch batch detail
			BatchDetail batchDetail = batchDetailRepository
					.findById(cartItem.getBatchDetailId())
					.orElseThrow(() -> new NotFoundException(
							"Product not found: " + cartItem.getBatchDetailId()));

			// Check stock
			if (batchDetail.getQuantity() < cartItem.getQuantity()) {
				throw new BadRequestException(
						"Insufficient stock for product: " + batchDetail.getBatchDetailId() +
								". Available: " + batchDetail.getQuantity() +
								", Requested: " + cartItem.getQuantity());
			}

			// Calculate item total
			BigDecimal itemTotal = batchDetail.getPrice()
					.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
			totalPrice = totalPrice.add(itemTotal);

			// Prepare order item
			OrderItem orderItem = new OrderItem();
			orderItem.setBatchDetailId(cartItem.getBatchDetailId());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setOriginalPrice(batchDetail.getPrice());
			orderItem.setUnitPriceAtPurchase(batchDetail.getPrice());

			orderItems.add(orderItem);
		}

		// 5. Create order
		Order order = new Order();
		order.setBuyerId(buyerId);
		order.setAddressId(addressId);
		order.setStatus(OrderStatus.PENDING);
		order.setType(Order.OrderType.DEFAULT);
		order.setTotalPrice(totalPrice.longValue()); // Convert to Long if needed

		Order savedOrder = orderRepository.save(order);
        
        // 6. Create order items and publish events
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getOrderId());
            OrderItem savedItem = orderItemRepository.save(item);
            savedOrderItems.add(savedItem);
            
            // Publish OrderItemCreatedEvent for each order item
            OrderItemCreatedEvent event = new OrderItemCreatedEvent(
                    savedItem.getOrderItemId(),
                    savedItem.getOrderId(),
                    savedItem.getBatchDetailId(),
                    savedItem.getQuantity(),
                    savedItem.getUnitPriceAtPurchase(),
                    buyerId
            );
            orderItemProducer.publishOrderItemCreated(event);
        }

		// 7. Update batch detail quantities
		for (CartItem cartItem : selectedItems) {
			BatchDetail batchDetail = batchDetailRepository
					.findById(cartItem.getBatchDetailId())
					.get();

			batchDetail.setQuantity(
					batchDetail.getQuantity() - cartItem.getQuantity().intValue());
			batchDetailRepository.save(batchDetail);
		}

		// 8. Delete selected cart items
		cartItemRepository.deleteByCartIdAndIsSelected(cart.getCartId(), true);

		// 9. Publish order created event (if exists)
		if (orderProducer != null) {
			orderProducer.publishOrderCreated(new OrderCreatedEvent(
					savedOrder.getOrderId(),
					savedOrder.getBuyerId(),
					savedOrder.getTotalPrice()
			));
		}

		return savedOrder;
	}
    
    @Override
    public OrderDetailResponse getOrderDetail(Long orderId) {
        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        // Get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // Build response
        return OrderDetailResponse.fromEntity(order, orderItems);
    }
}
