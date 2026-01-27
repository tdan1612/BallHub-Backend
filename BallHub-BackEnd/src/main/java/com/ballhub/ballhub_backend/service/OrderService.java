package com.ballhub.ballhub_backend.service;

import com.ballhub.ballhub_backend.dto.reponse.order.OrderDetailResponse;
import com.ballhub.ballhub_backend.dto.reponse.order.OrderItemResponse;
import com.ballhub.ballhub_backend.dto.reponse.order.OrderResponse;
import com.ballhub.ballhub_backend.dto.reponse.order.OrderStatusHistoryResponse;
import com.ballhub.ballhub_backend.dto.request.order.CreateOrderRequest;
import com.ballhub.ballhub_backend.entity.*;
import com.ballhub.ballhub_backend.exception.BadRequestException;
import com.ballhub.ballhub_backend.exception.ResourceNotFoundException;
import com.ballhub.ballhub_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private OrderStatusRepository statusRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    public OrderDetailResponse createOrder(Integer userId, CreateOrderRequest request) {
        // 1. Get cart
        Cart cart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // 2. Validate address
        UserAddress address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tồn tại"));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Địa chỉ không thuộc về bạn");
        }

        // 3. Validate payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentMethodIdAndIsActiveTrue(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Phương thức thanh toán không hợp lệ"));

        // 4. Check stock for all items
        for (CartItem item : cart.getItems()) {
            if (!item.getVariant().hasStock(item.getQuantity())) {
                throw new BadRequestException(
                        "Sản phẩm '" + item.getVariant().getProduct().getProductName() +
                                "' không đủ tồn kho. Còn lại: " + item.getVariant().getStockQuantity()
                );
            }
        }

        // 5. Get PENDING status
        OrderStatus pendingStatus = statusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("OrderStatus PENDING không tồn tại"));

        // 6. Create order
        Order order = Order.builder()
                .user(cart.getUser())
                .address(address)
                .paymentMethod(paymentMethod)
                .status(pendingStatus)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 7. Create order items and decrease stock
        for (CartItem cartItem : cart.getItems()) {
            // Create order item with snapshot
            OrderItem orderItem = OrderItem.fromCartItem(cartItem, savedOrder);
            savedOrder.getItems().add(orderItem);

            // Decrease stock
            cartItem.getVariant().decreaseStock(cartItem.getQuantity());
            variantRepository.save(cartItem.getVariant());
        }

        // 8. Calculate total amount
        savedOrder.calculateTotalAmount();

        // 9. Create status history
        savedOrder.updateStatus(pendingStatus, "Đơn hàng được tạo");

        // 10. Clear cart
        cart.clearCart();
        cartRepository.save(cart);

        // 11. Save order
        Order finalOrder = orderRepository.save(savedOrder);

        return mapToDetailResponse(finalOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByUserUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Integer userId, Integer orderId) {
        Order order = orderRepository.findByOrderIdAndUserUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        return mapToDetailResponse(order);
    }

    public void cancelOrder(Integer userId, Integer orderId) {
        Order order = orderRepository.findByOrderIdAndUserUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        // Check if order can be cancelled
        String currentStatus = order.getStatus().getStatusName();
        if (!"PENDING".equals(currentStatus) && !"CONFIRMED".equals(currentStatus)) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái: " + currentStatus);
        }

        // Get CANCELLED status
        OrderStatus cancelledStatus = statusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new RuntimeException("OrderStatus CANCELLED không tồn tại"));

        // Restore stock
        for (OrderItem item : order.getItems()) {
            item.getVariant().increaseStock(item.getQuantity());
            variantRepository.save(item.getVariant());
        }

        // Update status
        order.updateStatus(cancelledStatus, "Đơn hàng bị hủy bởi khách hàng");
        orderRepository.save(order);
    }

    // Mapping methods
    private OrderResponse mapToResponse(Order order) {
        int totalItems = order.getItems() != null
                ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
                : 0;

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .statusName(order.getStatus().getStatusName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .totalItems(totalItems)
                .paymentMethodName(order.getPaymentMethod().getMethodName())
                .build();
    }

    private OrderDetailResponse mapToDetailResponse(Order order) {
        List<OrderItem> items = order.getItems();
        if (items == null) {
            items = new java.util.ArrayList<>();
        }

        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        List<OrderStatusHistory> history = order.getStatusHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }

        List<OrderStatusHistoryResponse> historyResponses = history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .userFullName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .deliveryAddress(order.getAddress().getFullAddress())
                .paymentMethodName(order.getPaymentMethod().getMethodName())
                .statusName(order.getStatus().getStatusName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .statusHistory(historyResponses)
                .build();
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {
        ProductVariant variant = item.getVariant();
        String imageUrl = null;

        if (variant.getProduct() != null && variant.getProduct().getImages() != null) {
            imageUrl = variant.getProduct().getImages().stream()
                    .filter(img -> img.getIsMain() != null && img.getIsMain())
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElse(null);
        }

        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .variantId(variant.getVariantId())
                .productName(variant.getProduct() != null ? variant.getProduct().getProductName() : null)
                .sizeName(variant.getSize() != null ? variant.getSize().getSizeName() : null)
                .colorName(variant.getColor() != null ? variant.getColor().getColorName() : null)
                .quantity(item.getQuantity())
                .originalPrice(item.getOriginalPrice())
                .discountPercent(item.getDiscountPercent())
                .finalPrice(item.getFinalPrice())
                .subtotal(item.getSubtotal())
                .imageUrl(imageUrl)
                .build();
    }

    private OrderStatusHistoryResponse mapToHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .statusName(history.getStatus().getStatusName())
                .changedAt(history.getChangedAt())
                .note(history.getNote())
                .build();
    }

    // ============================================
    // ADMIN METHODS
    // ============================================

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetailAdmin(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        return mapToDetailResponse(order);
    }

    public OrderDetailResponse updateOrderStatus(Integer orderId, Integer statusId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        OrderStatus newStatus = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Trạng thái không tồn tại"));

        // Validate status transition
        String currentStatus = order.getStatus().getStatusName();
        String targetStatus = newStatus.getStatusName();

        validateStatusTransition(currentStatus, targetStatus);

        // Update status
        order.updateStatus(newStatus, note != null ? note : "Admin cập nhật trạng thái");
        Order updated = orderRepository.save(order);

        return mapToDetailResponse(updated);
    }

    private void validateStatusTransition(String currentStatus, String targetStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case "PENDING":
                if (!"CONFIRMED".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                    throw new BadRequestException(
                            "Không thể chuyển từ PENDING sang " + targetStatus
                    );
                }
                break;
            case "CONFIRMED":
                if (!"SHIPPING".equals(targetStatus) && !"CANCELLED".equals(targetStatus)) {
                    throw new BadRequestException(
                            "Không thể chuyển từ CONFIRMED sang " + targetStatus
                    );
                }
                break;
            case "SHIPPING":
                if (!"DELIVERED".equals(targetStatus)) {
                    throw new BadRequestException(
                            "Không thể chuyển từ SHIPPING sang " + targetStatus + ". Chỉ có thể chuyển sang DELIVERED"
                    );
                }
                break;
            case "DELIVERED":
                throw new BadRequestException("Không thể thay đổi trạng thái đơn hàng đã giao");
            case "CANCELLED":
                throw new BadRequestException("Không thể thay đổi trạng thái đơn hàng đã hủy");
            default:
                throw new BadRequestException("Trạng thái không hợp lệ: " + currentStatus);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String statusName) {
        return orderRepository.findByStatusStatusName(statusName).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}