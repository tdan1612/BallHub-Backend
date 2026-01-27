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

    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        ProductVariant variant = cartItem.getVariant();
        BigDecimal originalPrice = variant.getPrice();
        BigDecimal finalPrice = variant.getFinalPrice();

        // Calculate discount percent
        Integer discountPercent = 0;
        if (variant.getDiscountPrice() != null && variant.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = originalPrice.subtract(finalPrice);
            discountPercent = discount.multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }

        return OrderItem.builder()
                .order(order)
                .variant(variant)
                .quantity(cartItem.getQuantity())
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .finalPrice(finalPrice)
                .build();
    }
}
