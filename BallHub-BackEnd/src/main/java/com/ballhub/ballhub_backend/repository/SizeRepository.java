package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    Optional<Size> findBySizeName(String sizeName);
    boolean existsBySizeName(String sizeName);
}
