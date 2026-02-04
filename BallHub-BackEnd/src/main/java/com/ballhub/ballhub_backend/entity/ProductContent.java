package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProductContents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContentID")
    private Integer contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private ProductContentType type;

    @Column(name = "Title", length = 200)
    private String title;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content; // HTML hoặc JSON string

    @Column(name = "SortOrder")
    private Integer sortOrder = 0;

    @Column(name = "Status")
    private Boolean status = true;
}

