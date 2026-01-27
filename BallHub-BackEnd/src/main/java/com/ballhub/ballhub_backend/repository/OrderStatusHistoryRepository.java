package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {

    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtDesc(Integer orderId);
}
