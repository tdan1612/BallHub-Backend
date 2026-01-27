package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByStatusTrue();

    Optional<Category> findByCategoryIdAndStatusTrue(Integer id);

    List<Category> findByParentIsNullAndStatusTrue();

    List<Category> findByParentCategoryIdAndStatusTrue(Integer parentId);

    Optional<Category> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);
}
