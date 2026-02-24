package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.promotion.PromotionResponse;
import com.ballhub.ballhub_backend.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // FE sẽ gọi API này ở màn hình Thanh toán (Checkout) để hiển thị ví voucher
    @GetMapping("/vouchers/valid")
    public ResponseEntity<List<PromotionResponse>> getValidVouchers() {
        List<PromotionResponse> vouchers = promotionService.getValidVouchers();
        return ResponseEntity.ok(vouchers);
    }
}