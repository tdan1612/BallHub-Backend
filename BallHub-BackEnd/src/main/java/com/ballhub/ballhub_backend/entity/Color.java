package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Colors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ColorID")
    private Integer colorId;

    @Column(name = "ColorName", length = 50, unique = true, nullable = false)
    private String colorName;

    @OneToMany(mappedBy = "color", fetch = FetchType.LAZY)
    private java.util.List<ProductVariant> variants = new java.util.ArrayList<>();
}
