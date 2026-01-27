package com.ballhub.ballhub_backend.dto.reponse.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    private Integer productId;
    private String productName;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private List<VariantResponse> variants;
    private List<ProductImageResponse> images;
    private Boolean status;
    private LocalDateTime createdAt;
}
