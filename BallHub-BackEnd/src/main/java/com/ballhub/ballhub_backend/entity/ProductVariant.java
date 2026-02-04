package com.ballhub.ballhub_backend.entity;

import com.ballhub.ballhub_backend.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ProductVariants",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_ProductVariant",
                columnNames = {"ProductID", "SizeID", "ColorID"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VariantID")
    private Integer variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SizeID", nullable = false)
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ColorID", nullable = false)
    private Color color;

    @Column(name = "Price", precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "DiscountPrice", precision = 18, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "StockQuantity")
    private Integer stockQuantity = 0;

    @Column(name = "SKU", length = 50, unique = true)
    private String sku;

    @Column(name = "Status")
    private Boolean status = true;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    // ===== BUSINESS =====
    public BigDecimal getFinalPrice() {
        return discountPrice != null ? discountPrice : price;
    }

    public boolean hasStock(Integer quantity) {
        return this.stockQuantity != null && this.stockQuantity >= quantity;
    }

    public void decreaseStock(Integer quantity) {
        if (!hasStock(quantity)) {
            throw new BadRequestException("Không đủ tồn kho cho sản phẩm này");
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stockQuantity =
                (this.stockQuantity != null ? this.stockQuantity : 0) + quantity;
    }
}

