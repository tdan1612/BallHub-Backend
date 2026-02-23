package com.ballhub.ballhub_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấp quyền: Bất kỳ URL nào bắt đầu bằng /uploads/ sẽ được trỏ tới thư mục uploads/ trong ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}