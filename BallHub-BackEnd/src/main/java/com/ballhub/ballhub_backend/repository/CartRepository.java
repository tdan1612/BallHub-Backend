package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// CartRepository
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUserUserId(Integer userId);

    boolean existsByUserUserId(Integer userId);
}
