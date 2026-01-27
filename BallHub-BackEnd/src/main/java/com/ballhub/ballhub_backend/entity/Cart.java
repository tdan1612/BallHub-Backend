package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartID")
    private Integer cartId;

    @OneToOne
    @JoinColumn(name = "UserID", unique = true)
    private User user;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Business methods
    public void addItem(ProductVariant variant, Integer quantity) {
        // Check if item already exists
        CartItem existingItem = items.stream()
                .filter(item -> item.getVariant().getVariantId().equals(variant.getVariantId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(this)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
            items.add(newItem);
        }
    }

    public void removeItem(Integer variantId) {
        items.removeIf(item -> item.getVariant().getVariantId().equals(variantId));
    }

    public void clearCart() {
        items.clear();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getVariant().getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}


