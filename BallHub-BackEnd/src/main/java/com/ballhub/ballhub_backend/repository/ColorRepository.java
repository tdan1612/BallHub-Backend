package com.ballhub.ballhub_backend.repository;

import com.ballhub.ballhub_backend.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    Optional<Color> findByColorName(String colorName);
    boolean existsByColorName(String colorName);
}
