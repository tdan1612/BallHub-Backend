package com.ballhub.ballhub_backend.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Address ID không được để trống")
    private Integer addressId;

    @NotNull(message = "Payment method ID không được để trống")
    private Integer paymentMethodId;

    private String note;
}
