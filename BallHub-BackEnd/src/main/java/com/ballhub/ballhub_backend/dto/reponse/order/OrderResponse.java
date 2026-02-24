package com.ballhub.ballhub_backend.dto.reponse.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Integer orderId;
    private Integer userId;
    private String statusName;
    private LocalDateTime orderDate;

    // --- BỔ SUNG CHO KHUYẾN MÃI ---
    private BigDecimal subTotal;       // Tổng tiền hàng trước giảm
    private BigDecimal discountAmount; // Số tiền được voucher giảm
    // ------------------------------

    private BigDecimal totalAmount;    // Tiền cuối cùng phải trả
    private Integer totalItems;
    private String paymentMethodName;
}