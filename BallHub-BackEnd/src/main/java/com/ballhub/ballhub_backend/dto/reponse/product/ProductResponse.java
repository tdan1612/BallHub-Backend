package com.ballhub.ballhub_backend.dto.reponse.product;

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
public class ProductResponse {

    private Integer productId;
    private String productName;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private String mainImage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean status;
    private LocalDateTime createdAt;
}
