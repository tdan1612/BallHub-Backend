package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AddressID")
    private UserAddress address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PaymentMethodID")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "StatusID")
    private OrderStatus status;

    // --- CÁC TRƯỜNG MỚI BỔ SUNG CHO VOUCHER ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionID")
    private Promotion promotion; // Mã voucher áp dụng cho đơn

    @Column(name = "SubTotal", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "DiscountAmount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    // ------------------------------------------

    @Column(name = "OrderDate", updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
    }

    // Business methods
    public void updateStatus(OrderStatus newStatus, String note) {
        this.status = newStatus;
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .status(newStatus)
                .changedAt(LocalDateTime.now())
                .note(note)
                .build();
        statusHistory.add(history);
    }

    public void calculateTotalAmount() {
        // 1. Tính tổng tiền hàng (Sau khi từng món đã được giảm lẻ 10, 20%)
        BigDecimal sub = items.stream()
                .map(item -> item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.subTotal = sub;

        // 2. Trừ thêm Voucher giảm giá (Nếu có)
        BigDecimal discount = (this.discountAmount != null) ? this.discountAmount : BigDecimal.ZERO;
        this.totalAmount = this.subTotal.subtract(discount);
    }
}