package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    List<UserAddress> findByUserUserId(Integer userId);
    List<UserAddress> findByUserUserIdAndIsDefaultTrue(Integer userId);
}
