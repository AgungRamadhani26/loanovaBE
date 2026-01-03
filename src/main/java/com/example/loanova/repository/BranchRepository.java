package com.example.loanova.repository;

import com.example.loanova.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query(value = "SELECT count(*) FROM branches WHERE branch_code = :branchCode", nativeQuery = true)
    long countBranchCodeNative(@Param("branchCode") String branchCode);

    @Query(value = "SELECT count(*) FROM branches WHERE branch_name = :branchName", nativeQuery = true)
    long countBranchNameNative(@Param("branchName") String branchName);

    default boolean existsByBranchCodeAnyStatus(String branchCode) {
        return countBranchCodeNative(branchCode) > 0;
    }

    default boolean existsByBranchNameAnyStatus(String branchName) {
        return countBranchNameNative(branchName) > 0;
    }

    // Standard JPA check (Active Only)
    boolean existsByBranchCode(String branchCode);
    boolean existsByBranchName(String branchName);

    // Native query to find branch even if soft deleted
    @Query(value = "SELECT * FROM branches WHERE id = :id", nativeQuery = true)
    Optional<Branch> findByIdIncludeDeleted(@Param("id") Long id);
}
