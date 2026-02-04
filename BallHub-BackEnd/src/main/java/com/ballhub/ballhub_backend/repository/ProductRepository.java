package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Product;
import com.ballhub.ballhub_backend.entity.ProductContent;
import com.ballhub.ballhub_backend.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    // =====================================================
    // BASIC
    // =====================================================

    Page<Product> findByStatusTrue(Pageable pageable);

    Optional<Product> findByProductIdAndStatusTrue(Integer id);

    // =====================================================
    // SEARCH
    // =====================================================

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = true
          AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
          AND (:brandId IS NULL OR p.brand.brandId = :brandId)
    """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            Pageable pageable
    );

    // =====================================================
    // SORT ONLY
    // =====================================================

    @Query("""
        SELECT p FROM Product p
        JOIN p.variants v
        WHERE p.status = true
          AND v.status = true
          AND v.stockQuantity > 0
        GROUP BY p
        ORDER BY MIN(COALESCE(v.discountPrice, v.price)) ASC
    """)
    Page<Product> findAllOrderByMinPriceAsc(Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        JOIN p.variants v
        WHERE p.status = true
          AND v.status = true
          AND v.stockQuantity > 0
        GROUP BY p
        ORDER BY MIN(COALESCE(v.discountPrice, v.price)) DESC
    """)
    Page<Product> findAllOrderByMinPriceDesc(Pageable pageable);

    // =====================================================
    // PRODUCT DETAIL (FIX MULTIPLE BAG FETCH)
    // =====================================================

    /**
     * Lấy product + variants
     * ❗ KHÔNG fetch images ở đây
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.variants v
        WHERE p.productId = :id
          AND p.status = true
    """)
    Optional<Product> findProductWithVariants(@Param("id") Integer id);

    // =====================================================
    // PRODUCT IMAGES (QUERY RIÊNG)
    // =====================================================

    @Query("""
        SELECT i FROM ProductImage i
        WHERE i.product.productId = :productId
    """)
    List<ProductImage> findImagesByProductId(@Param("productId") Integer productId);

    // =====================================================
    // FILTER + SORT + PAGING (SHOP CORE)
    // =====================================================

    @Query(value = """
        SELECT p.* FROM Products p
        JOIN ProductVariants v ON p.ProductID = v.ProductID
        JOIN Categories c ON p.CategoryID = c.CategoryID
        JOIN Brands b ON p.BrandID = b.BrandID
        JOIN Sizes s ON v.SizeID = s.SizeID
        WHERE p.Status = 1
          AND v.Status = 1
          AND v.StockQuantity > 0

          AND (:categories IS NULL OR c.CategoryName IN (:categories))
          AND (:teams IS NULL OR b.BrandName IN (:teams))
          AND (:sizes IS NULL OR s.SizeName IN (:sizes))

          AND (:minPrice IS NULL OR COALESCE(v.DiscountPrice, v.Price) >= :minPrice)
          AND (:maxPrice IS NULL OR COALESCE(v.DiscountPrice, v.Price) <= :maxPrice)

        GROUP BY p.ProductID, p.ProductName, p.Description,
                 p.CategoryID, p.BrandID, p.Status, p.CreatedAt

        ORDER BY
          CASE WHEN :sort = 'new' THEN p.CreatedAt END DESC,
          CASE WHEN :sort = 'price_asc' THEN MIN(COALESCE(v.DiscountPrice, v.Price)) END ASC,
          CASE WHEN :sort = 'price_desc' THEN MIN(COALESCE(v.DiscountPrice, v.Price)) END DESC,
          p.ProductID DESC
        """,

            countQuery = """
        SELECT COUNT(DISTINCT p.ProductID)
        FROM Products p
        JOIN ProductVariants v ON p.ProductID = v.ProductID
        JOIN Categories c ON p.CategoryID = c.CategoryID
        JOIN Brands b ON p.BrandID = b.BrandID
        JOIN Sizes s ON v.SizeID = s.SizeID
        WHERE p.Status = 1
          AND v.Status = 1
          AND v.StockQuantity > 0

          AND (:categories IS NULL OR c.CategoryName IN (:categories))
          AND (:teams IS NULL OR b.BrandName IN (:teams))
          AND (:sizes IS NULL OR s.SizeName IN (:sizes))
          AND (:minPrice IS NULL OR COALESCE(v.DiscountPrice, v.Price) >= :minPrice)
          AND (:maxPrice IS NULL OR COALESCE(v.DiscountPrice, v.Price) <= :maxPrice)
        """,

            nativeQuery = true
    )
    Page<Product> filterNativeShop(
            @Param("categories") List<String> categories,
            @Param("teams") List<String> teams,
            @Param("sizes") List<String> sizes,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("sort") String sort,
            Pageable pageable
    );
}
