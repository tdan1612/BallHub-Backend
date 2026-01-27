package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProductImages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageID")
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VariantID")
    private ProductVariant variant;

    @Column(name = "ImageUrl", length = 255)
    private String imageUrl;

    @Column(name = "IsMain")
    private Boolean isMain = false;
}
