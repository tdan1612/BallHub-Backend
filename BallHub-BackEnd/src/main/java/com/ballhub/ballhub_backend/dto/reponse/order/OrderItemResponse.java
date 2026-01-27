package com.ballhub.ballhub_backend.dto.reponse.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer orderItemId;
    private Integer variantId;
    private String productName;
    private String sizeName;
    private String colorName;
    private Integer quantity;
    private BigDecimal originalPrice;
    private Integer discountPercent;
    private BigDecimal finalPrice;
    private BigDecimal subtotal;
    private String imageUrl;
}
