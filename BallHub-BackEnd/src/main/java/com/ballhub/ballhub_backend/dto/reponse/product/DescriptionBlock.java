package com.ballhub.ballhub_backend.dto.reponse.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DescriptionBlock {
    private String html;   // HTML render thẳng
}
