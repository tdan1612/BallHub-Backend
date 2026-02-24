package com.ballhub.ballhub_backend.dto.reponse.promotion;

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
public class PromotionResponse {
    private Integer promotionId;
    private String promotionName;     // Tên chương trình (VD: "Chào bạn mới")
    private String promoCode;         // Mã để nhập (VD: "BALLHUB20")
    private Integer discountPercent;  // % giảm
    private String discountType;      // PERCENT hoặc FIXED
    private BigDecimal minOrderAmount;    // Đơn tối thiểu để dùng
    private BigDecimal maxDiscountAmount; // Giảm tối đa
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}