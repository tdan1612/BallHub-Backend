package com.ballhub.ballhub_backend.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVariantRequest {

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Giá khuyến mãi phải >= 0")
    private BigDecimal discountPrice;

    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;

    private Boolean status;
}