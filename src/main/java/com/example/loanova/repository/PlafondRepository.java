package com.example.loanova.repository;

import com.example.loanova.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {
  
  /* Mencari plafond berdasarkan nama (Active Only) */
  Optional<Plafond> findByName(String name);

  /* Menghitung jumlah plafond dengan nama tertentu di seluruh database (termasuk yang di-soft delete) */
  @Query(value = "SELECT count(*) FROM plafonds WHERE name = :name", nativeQuery = true)
  long countNameNative(@Param("name") String name);

  /* Mengecek keberadaan nama plafond di sistem tanpa mempedulikan status delete */
  default boolean existsByNameAnyStatus(String name) {
    return countNameNative(name) > 0;
  }

  /* Standard JPA check (Hanya yang aktif) */
  boolean existsByName(String name);

  /* Query native untuk menemukan plafond termasuk yang sudah di-soft delete */
  @Query(value = "SELECT * FROM plafonds WHERE id = :id", nativeQuery = true)
  Optional<Plafond> findByIdIncludeDeleted(@Param("id") Long id);
}
