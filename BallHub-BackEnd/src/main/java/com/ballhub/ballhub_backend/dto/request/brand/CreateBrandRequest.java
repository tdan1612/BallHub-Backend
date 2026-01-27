package com.ballhub.ballhub_backend.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu không quá 100 ký tự")
    private String brandName;

    @Size(max = 255, message = "Description không quá 255 ký tự")
    private String description;

    private String logo;
}
