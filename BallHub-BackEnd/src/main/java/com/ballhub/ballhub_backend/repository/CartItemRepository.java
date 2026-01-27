package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartCartIdAndVariantVariantId(Integer cartId, Integer variantId);

    void deleteByCartCartIdAndVariantVariantId(Integer cartId, Integer variantId);
}
