package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByPromoCode(String promoCode);

    // Lọc ra các VOUCHER (có mã) đang hợp lệ để cho khách chọn
    @Query("SELECT p FROM Promotion p WHERE p.promoCode IS NOT NULL " +
            "AND p.status = true " +
            "AND (p.startDate IS NULL OR p.startDate <= CURRENT_TIMESTAMP) " +
            "AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP) " +
            "AND (p.usageLimit IS NULL OR p.usageLimit = 0 OR p.usedCount < p.usageLimit)")
    List<Promotion> findValidVouchers();
}