package com.ballhub.ballhub_backend.dto.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentMethodRequest {

    @NotBlank(message = "Tên phương thức thanh toán không được để trống")
    @Size(max = 100, message = "Tên không quá 100 ký tự")
    private String methodName;

    private Boolean isActive;
}
