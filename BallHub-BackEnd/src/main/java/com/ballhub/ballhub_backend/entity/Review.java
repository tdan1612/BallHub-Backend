package com.ballhub.ballhub_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private Integer reviewId;

    @Column(name = "ProductID", nullable = false)
    private Integer productId;

    @Column(name = "UserID", nullable = false)
    private Integer userId;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Comment")
    private String comment;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Status")
    private Boolean status;
}
