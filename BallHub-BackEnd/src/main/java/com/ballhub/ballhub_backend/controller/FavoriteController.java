package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.ApiResponse;
import com.ballhub.ballhub_backend.dto.reponse.PageResponse;
import com.ballhub.ballhub_backend.dto.reponse.product.ProductResponse;
import com.ballhub.ballhub_backend.security.CustomUserDetails;
import com.ballhub.ballhub_backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // 1. Lấy danh sách sản phẩm yêu thích của tôi
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication) {

        Integer userId = getUserId(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductResponse> products = favoriteService.getMyFavorites(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(products)));
    }

    // 2. Thêm hoặc Bỏ yêu thích (Toggle)
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @PathVariable Integer productId,
            Authentication authentication) {

        Integer userId = getUserId(authentication);
        boolean isFavorited = favoriteService.toggleFavorite(userId, productId);

        String message = isFavorited ? "Đã thêm vào danh sách yêu thích" : "Đã bỏ khỏi danh sách yêu thích";
        return ResponseEntity.ok(ApiResponse.success(message, isFavorited));
    }

    // 3. Kiểm tra xem 1 sản phẩm đã được thả tim chưa (Dùng khi load trang chi tiết sản phẩm)
    @GetMapping("/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @PathVariable Integer productId,
            Authentication authentication) {

        Integer userId = getUserId(authentication);
        boolean isFavorited = favoriteService.checkFavorite(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(isFavorited));
    }

    private Integer getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}