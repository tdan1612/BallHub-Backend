package com.ballhub.ballhub_backend.dto.request.review;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    private Integer rating;
    private String comment;
}
