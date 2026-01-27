package com.ballhub.ballhub_backend.repository.spec;

import com.ballhub.ballhub_backend.entity.Product;
import com.ballhub.ballhub_backend.entity.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(
            List<String> categories,
            List<String> teams,
            List<String> sizes,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {

            query.distinct(true);

            Join<Product, ProductVariant> variantJoin =
                    root.join("variants", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            // product đang bán
            predicates.add(cb.isTrue(root.get("status")));

            // variant đang bán + còn hàng
            predicates.add(cb.isTrue(variantJoin.get("status")));
            predicates.add(cb.greaterThan(variantJoin.get("stockQuantity"), 0));

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("categoryName").in(categories));
            }

            if (teams != null && !teams.isEmpty()) {
                predicates.add(root.get("brand").get("brandName").in(teams));
            }

            if (sizes != null && !sizes.isEmpty()) {
                predicates.add(variantJoin.get("size").get("sizeName").in(sizes));
            }

            if (minPrice != null && maxPrice != null) {

                Expression<BigDecimal> finalPrice =
                        cb.coalesce(
                                variantJoin.get("discountPrice"),
                                variantJoin.get("price")
                        );

                predicates.add(cb.between(finalPrice, minPrice, maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

