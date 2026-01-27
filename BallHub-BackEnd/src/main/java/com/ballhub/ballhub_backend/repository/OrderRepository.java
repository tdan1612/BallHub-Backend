package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUserUserId(Integer userId, Pageable pageable);

    Optional<Order> findByOrderIdAndUserUserId(Integer orderId, Integer userId);

    List<Order> findByStatusStatusName(String statusName);
}
