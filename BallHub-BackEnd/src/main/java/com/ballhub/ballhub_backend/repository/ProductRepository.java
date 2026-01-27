package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    Page<Product> findByStatusTrue(Pageable pageable);

    Optional<Product> findByProductIdAndStatusTrue(Integer id);

    @Query("SELECT p FROM Product p " +
            "WHERE p.status = true " +
            "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.brandId = :brandId)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            Pageable pageable
    );
}
