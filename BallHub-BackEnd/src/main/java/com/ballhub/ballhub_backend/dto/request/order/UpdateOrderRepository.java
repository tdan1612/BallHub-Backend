package com.ballhub.ballhub_backend.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class UpdateOrderStatusRequest {

    @NotNull(message = "Status ID không được để trống")
    private Integer statusId;

    private String note;
}
