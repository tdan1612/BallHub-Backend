package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Review;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("""
        SELECT r FROM Review r
        WHERE r.productId = :productId AND r.status = true
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByProductId(@Param("productId") Integer productId);

    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.productId = :productId AND r.status = true
    """)
    Double getAvgRating(@Param("productId") Integer productId);

    @Query("""
        SELECT COUNT(r) FROM Review r
        WHERE r.productId = :productId AND r.status = true
    """)
    Long getTotalReviews(@Param("productId") Integer productId);

    boolean existsByProductIdAndUserId(Integer productId, Integer userId);
}
