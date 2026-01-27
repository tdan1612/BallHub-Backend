package com.ballhub.ballhub_backend.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariantRequest {

    @NotNull(message = "Size không được để trống")
    private Integer sizeId;

    @NotNull(message = "Color không được để trống")
    private Integer colorId;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity = 0;

    @Size(max = 50, message = "SKU không quá 50 ký tự")
    private String sku;
}
