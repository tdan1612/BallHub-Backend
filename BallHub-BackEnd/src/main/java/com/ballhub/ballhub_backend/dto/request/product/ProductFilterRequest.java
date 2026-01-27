package com.ballhub.ballhub_backend.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private String keyword;
    private Integer categoryId;
    private Integer brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
