package com.ballhub.ballhub_backend.dto.reponse.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {

    private Integer orderId;
    private Integer userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private String deliveryAddress;
    private String paymentMethodName;
    private String statusName;
    private LocalDateTime orderDate;

    // --- BỔ SUNG CHI TIẾT BIÊN LAI ---
    private BigDecimal subTotal;       // Tổng tiền hàng hóa
    private BigDecimal discountAmount; // Số tiền được trừ từ Voucher
    private String promoCode;          // Tên mã Voucher đã dùng (để FE hiển thị "Đã áp dụng mã BALLHUB20")
    // ---------------------------------

    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;
}