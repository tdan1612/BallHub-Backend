package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderItemID")
    private Integer orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VariantID", nullable = false)
    private ProductVariant variant;

    // --- TRƯỜNG MỚI BỔ SUNG CHO FLASH SALE SẢN PHẨM ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AppliedPromotionID")
    private Promotion appliedPromotion; // Lưu CTKM áp dụng cho riêng sản phẩm này
    // ---------------------------------------------------

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "OriginalPrice", precision = 18, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "DiscountPercent")
    private Integer discountPercent = 0;

    @Column(name = "FinalPrice", precision = 18, scale = 2, nullable = false)
    private BigDecimal finalPrice;

    // Business methods
    public BigDecimal getSubtotal() {
        return finalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Hàm này mình để nguyên chữ ký, nhưng logic sẽ cần bạn truyền thêm
    // thông tin Promotion từ DTO/CartItem vào khi gọi ở OrderService
    public static OrderItem fromCartItem(CartItem cartItem, Order order, Promotion appliedPromo, BigDecimal finalPriceCalculated) {
        ProductVariant variant = cartItem.getVariant();
        BigDecimal originalPrice = variant.getPrice();

        // Tính % giảm giá nếu có
        Integer discountPct = 0;
        if (appliedPromo != null && appliedPromo.getDiscountPercent() != null) {
            discountPct = appliedPromo.getDiscountPercent();
        }

        return OrderItem.builder()
                .order(order)
                .variant(variant)
                .appliedPromotion(appliedPromo)
                .quantity(cartItem.getQuantity())
                .originalPrice(originalPrice)
                .discountPercent(discountPct)
                .finalPrice(finalPriceCalculated != null ? finalPriceCalculated : originalPrice)
                .build();
    }
}