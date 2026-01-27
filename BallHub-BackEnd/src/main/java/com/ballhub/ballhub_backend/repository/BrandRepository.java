package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    List<Brand> findByStatusTrue();

    Optional<Brand> findByBrandIdAndStatusTrue(Integer id);

    Optional<Brand> findByBrandName(String brandName);

    boolean existsByBrandName(String brandName);
}
