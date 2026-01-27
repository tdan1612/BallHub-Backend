package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    List<PaymentMethod> findByIsActiveTrue();

    Optional<PaymentMethod> findByPaymentMethodIdAndIsActiveTrue(Integer id);
}