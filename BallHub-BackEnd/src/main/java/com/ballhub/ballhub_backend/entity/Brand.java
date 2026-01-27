package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "BrandName", length = 100, unique = true, nullable = false)
    private String brandName;

    @Column(name = "Logo", length = 255)
    private String logo;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Status")
    private Boolean status = true;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}
