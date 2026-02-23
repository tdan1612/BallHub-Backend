package com.ballhub.ballhub_backend.service;

import com.ballhub.ballhub_backend.dto.reponse.promotion.PromotionResponse;
import com.ballhub.ballhub_backend.entity.Promotion;
import com.ballhub.ballhub_backend.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<PromotionResponse> getValidVouchers() {
        // Lấy danh sách Voucher hợp lệ từ DB và chuyển sang DTO
        return promotionRepository.findValidVouchers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .promotionName(promotion.getPromotionName())
                .promoCode(promotion.getPromoCode())
                .discountPercent(promotion.getDiscountPercent())
                .discountType(promotion.getDiscountType())
                .minOrderAmount(promotion.getMinOrderAmount())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .build();
    }
}