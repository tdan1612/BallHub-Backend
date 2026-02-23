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
import java.math.RoundingMode;
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

    @Autowired
    private PromotionRepository promotionRepository;

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

        // 5. Check Voucher (PromoCode) if provided in request
        Promotion appliedVoucher = null;
        if (request.getPromoCode() != null && !request.getPromoCode().trim().isEmpty()) {
            appliedVoucher = promotionRepository.findByPromoCode(request.getPromoCode())
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại"));

            if (!appliedVoucher.isValid()) {
                throw new BadRequestException("Mã giảm giá đã hết hạn hoặc hết lượt sử dụng");
            }
        }

        // 6. Get PENDING status
        OrderStatus pendingStatus = statusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new RuntimeException("OrderStatus PENDING không tồn tại"));

        // 7. Initialize Order
        Order order = Order.builder()
                .user(cart.getUser())
                .address(address)
                .paymentMethod(paymentMethod)
                .status(pendingStatus)
                .promotion(appliedVoucher) // Gắn Voucher vào đơn hàng
                .build();

        // Save tạm để lấy OrderID gắn cho OrderItem
        Order savedOrder = orderRepository.save(order);

        // 8. Process items, calculate product flash sales, and decrease stock
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();

            // Xử lý giá Flash Sale của từng sản phẩm (nếu có)
            Promotion itemPromo = null;
            BigDecimal finalPrice = variant.getPrice(); // Bạn có thể tính finalPrice ở đây nếu có itemPromo

            // Gọi hàm fromCartItem đã được sửa ở Entity
            OrderItem orderItem = OrderItem.fromCartItem(cartItem, savedOrder, itemPromo, finalPrice);
            savedOrder.getItems().add(orderItem);

            // Giảm tồn kho
            variant.decreaseStock(cartItem.getQuantity());
            variantRepository.save(variant);
        }

        // 9. Calculate Order Total (SubTotal)
        savedOrder.calculateTotalAmount(); // Hàm này sẽ tính SubTotal từ các OrderItem

        // 10. Apply Voucher Discount Logic
        if (appliedVoucher != null) {
            BigDecimal subTotal = savedOrder.getSubTotal();

            // Kiểm tra điều kiện đơn tối thiểu
            if (subTotal.compareTo(appliedVoucher.getMinOrderAmount()) < 0) {
                throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này (" + appliedVoucher.getMinOrderAmount() + ")");
            }

            BigDecimal discountAmt = BigDecimal.ZERO;
            if ("PERCENT".equals(appliedVoucher.getDiscountType())) {
                discountAmt = subTotal.multiply(BigDecimal.valueOf(appliedVoucher.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                // Giới hạn số tiền giảm tối đa
                if (appliedVoucher.getMaxDiscountAmount() != null && discountAmt.compareTo(appliedVoucher.getMaxDiscountAmount()) > 0) {
                    discountAmt = appliedVoucher.getMaxDiscountAmount();
                }
            } else if ("FIXED".equals(appliedVoucher.getDiscountType())) {
                discountAmt = appliedVoucher.getMaxDiscountAmount(); // Fixed amount lưu ở maxDiscountAmount
            }

            savedOrder.setDiscountAmount(discountAmt);
            // Tính lại TotalAmount sau khi có Voucher
            savedOrder.calculateTotalAmount();

            // Cập nhật số lượt dùng của Voucher
            appliedVoucher.setUsedCount(appliedVoucher.getUsedCount() + 1);
            promotionRepository.save(appliedVoucher);
        }

        // 11. Create status history
        savedOrder.updateStatus(pendingStatus, "Đơn hàng được tạo");

        // 12. Clear cart
        cart.clearCart();
        cartRepository.save(cart);

        // 13. Save final order
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

    // ============================================
    // MAPPING METHODS
    // ============================================
    private OrderResponse mapToResponse(Order order) {
        int totalItems = order.getItems() != null
                ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
                : 0;

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .statusName(order.getStatus().getStatusName())
                .orderDate(order.getOrderDate())
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
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

        // Lấy mã giảm giá nếu đơn này có dùng
        String promoCodeUsed = null;
        if (order.getPromotion() != null) {
            promoCodeUsed = order.getPromotion().getPromoCode();
        }

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .userFullName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .deliveryAddress(order.getAddress().getFullAddress())
                .paymentMethodName(order.getPaymentMethod().getMethodName())
                .statusName(order.getStatus().getStatusName())
                .orderDate(order.getOrderDate())
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
                .promoCode(promoCodeUsed)
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

        // Lấy tên CTKM nếu sản phẩm này được giảm giá trực tiếp
        String promotionName = null;
        if (item.getAppliedPromotion() != null) {
            promotionName = item.getAppliedPromotion().getPromotionName();
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
                .appliedPromotionName(promotionName)
                .finalPrice(item.getFinalPrice())
                .subtotal(item.getSubtotal())
                .imageUrl(imageUrl)
                .build();
    }

    // Hàm bị thiếu đã được thêm vào đây
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