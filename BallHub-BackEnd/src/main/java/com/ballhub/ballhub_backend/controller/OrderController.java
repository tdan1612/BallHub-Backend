package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.ApiResponse;
import com.ballhub.ballhub_backend.dto.reponse.PageResponse;
import com.ballhub.ballhub_backend.dto.reponse.order.OrderDetailResponse;
import com.ballhub.ballhub_backend.dto.reponse.order.OrderResponse;
import com.ballhub.ballhub_backend.dto.request.order.CreateOrderRequest;
import com.ballhub.ballhub_backend.security.CustomUserDetails;
import com.ballhub.ballhub_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        Integer userId = getUserId(authentication);

        // 1. Lưu đơn hàng vào Database qua OrderService
        OrderDetailResponse order = orderService.createOrder(userId, request);

        // 2. Kiểm tra phương thức thanh toán
        // Giả sử ID = 2 là "Chuyển khoản ngân hàng"
        if (request.getPaymentMethodId() != null && request.getPaymentMethodId() == 2) {
            try {
                // Cú pháp: https://img.vietqr.io/image/{Mã_NH}-{STK}-compact2.jpg?amount={Tiền}&addInfo={Nội_dung}&accountName={Tên_chủ_TK}
                // TẠO LINK ẢNH MÃ QR BẰNG VIETQR (Dùng STK thật của bạn)
                String paymentUrl = "https://img.vietqr.io/image/MB-0886301661-compact2.jpg?amount="
                        + order.getTotalAmount().intValue()
                        + "&addInfo=THANH TOAN DON HANG " + order.getOrderId()
                        + "&accountName=NGO GIA HIEN"; // Bạn có thể đổi thành BALLHUB STORE hoặc tên thật viết không dấu nhé

                // Trả về dữ liệu gồm cả chi tiết đơn hàng và link ảnh mã QR
                Map<String, Object> result = new HashMap<>();
                result.put("order", order);
                result.put("paymentUrl", paymentUrl);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Vui lòng quét mã QR", result));

            } catch (Exception e) {
                throw new RuntimeException("Lỗi tạo link thanh toán: " + e.getMessage());
            }
        }

        // 3. Nếu là COD (Thanh toán khi nhận hàng)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderResponse> orders = orderService.getMyOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        OrderDetailResponse order = orderService.getOrderDetail(userId, id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        orderService.cancelOrder(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy đơn hàng", null));
    }

    private Integer getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}