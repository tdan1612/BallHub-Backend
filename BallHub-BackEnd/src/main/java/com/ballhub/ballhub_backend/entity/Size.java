package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SizeID")
    private Integer sizeId;

    @Column(name = "SizeName", length = 10, unique = true, nullable = false)
    private String sizeName;

    @OneToMany(mappedBy = "size", fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();
}
