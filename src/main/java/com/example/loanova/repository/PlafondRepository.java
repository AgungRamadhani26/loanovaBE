package com.example.loanova.repository;

import com.example.loanova.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {
    Optional<Plafond> findByName(String name);
}
