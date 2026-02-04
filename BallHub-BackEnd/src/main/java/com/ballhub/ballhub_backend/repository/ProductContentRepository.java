package com.ballhub.ballhub_backend.repository;


import com.ballhub.ballhub_backend.entity.ProductContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductContentRepository
        extends JpaRepository<ProductContent, Integer> {

    List<ProductContent>
    findByProduct_ProductIdAndStatusTrueOrderBySortOrderAsc(Integer productId);
}
