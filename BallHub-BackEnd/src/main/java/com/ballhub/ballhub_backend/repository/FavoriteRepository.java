package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    // Lấy danh sách yêu thích của User (có phân trang)
    Page<Favorite> findByUserUserId(Integer userId, Pageable pageable);

    // Kiểm tra xem User đã thả tim sản phẩm này chưa
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);

    // Tìm record để xóa
    Optional<Favorite> findByUserUserIdAndProductProductId(Integer userId, Integer productId);
}