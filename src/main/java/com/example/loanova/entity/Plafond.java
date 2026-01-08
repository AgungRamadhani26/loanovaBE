package com.example.loanova.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * PLAFOND ENTITY Merepresentasikan paket plafon pinjaman beserta suku bunga dan tenornya.
 * Meng-extend BaseEntity untuk mendapatkan fungsionalitas audit dan soft delete.
 */
@Entity
@Table(name = "plafonds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Plafond extends BaseEntity {

  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name; // Nama paket plafon (misal: "Gold")

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal maxAmount;

  @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal interestRate;

  @Column(name = "tenor_min", nullable = false)
  private Integer tenorMin;

  @Column(name = "tenor_max", nullable = false)
  private Integer tenorMax;
}
