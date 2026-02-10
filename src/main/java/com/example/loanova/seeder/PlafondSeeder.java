package com.example.loanova.seeder;

import com.example.loanova.entity.Plafond;
import com.example.loanova.repository.PlafondRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Run after SuperAdminSeeder
public class PlafondSeeder implements CommandLineRunner {

  private final PlafondRepository plafondRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    seedPlafonds();
  }

  private void seedPlafonds() {
    List<PlafondData> plafondsToSeed =
        Arrays.asList(
            new PlafondData(
                "Bronze",
                "Paket plafon Bronze dengan limit pinjaman kecil dan bunga standar",
                new BigDecimal("13000000"),
                new BigDecimal("1.5"),
                4,
                12),
            new PlafondData(
                "Silver",
                "Paket plafon Silver dengan limit menengah dan bunga kompetitif",
                new BigDecimal("20000000"),
                new BigDecimal("1.25"),
                4,
                16),
            new PlafondData(
                "Gold",
                "Paket plafon Gold dengan limit besar dan bunga lebih rendah",
                new BigDecimal("28000000"),
                new BigDecimal("1.0"),
                6,
                20));

    for (PlafondData data : plafondsToSeed) {
      upsertPlafond(data);
    }
  }

  private void upsertPlafond(PlafondData data) {
    Optional<Plafond> existingPlafond = plafondRepository.findByName(data.name);

    // If not found by exact name, try finding by uppercase name (to handle legacy data)
    if (existingPlafond.isEmpty()) {
      existingPlafond = plafondRepository.findByName(data.name.toUpperCase());
    }

    if (existingPlafond.isPresent()) {
      Plafond plafond = existingPlafond.get();
      log.info("Updating existing Plafond: {}", plafond.getName());

      // Update fields
      plafond.setName(data.name); // Normalize name to "Bronze", "Silver", etc.
      plafond.setDescription(data.description);
      plafond.setMaxAmount(data.maxAmount);
      plafond.setInterestRate(data.interestRate);
      plafond.setTenorMin(data.tenorMin);
      plafond.setTenorMax(data.tenorMax);

      plafondRepository.save(plafond);
    } else {
      log.info("Creating new Plafond: {}", data.name);
      Plafond newPlafond =
          Plafond.builder()
              .name(data.name)
              .description(data.description)
              .maxAmount(data.maxAmount)
              .interestRate(data.interestRate)
              .tenorMin(data.tenorMin)
              .tenorMax(data.tenorMax)
              .build();
      plafondRepository.save(newPlafond);
    }
  }

  // Simple inner record/class to hold data
  private record PlafondData(
      String name,
      String description,
      BigDecimal maxAmount,
      BigDecimal interestRate,
      Integer tenorMin,
      Integer tenorMax) {}
}
