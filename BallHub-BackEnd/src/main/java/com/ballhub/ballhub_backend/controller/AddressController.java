package com.ballhub.ballhub_backend.controller;

import com.ballhub.ballhub_backend.dto.reponse.ApiResponse;
import com.ballhub.ballhub_backend.dto.reponse.user.AddressResponse;
import com.ballhub.ballhub_backend.dto.request.user.CreateAddressRequest;
import com.ballhub.ballhub_backend.dto.request.user.UpdateAddressRequest;
import com.ballhub.ballhub_backend.security.CustomUserDetails;
import com.ballhub.ballhub_backend.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;  

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(Authentication authentication) {
        Integer userId = getUserId(authentication);
        List<AddressResponse> addresses = addressService.getMyAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        AddressResponse address = addressService.createAddress(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo địa chỉ thành công", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        AddressResponse address = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer userId = getUserId(authentication);
        AddressResponse address = addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã đặt làm địa chỉ mặc định", address));
    }

    private Integer getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
