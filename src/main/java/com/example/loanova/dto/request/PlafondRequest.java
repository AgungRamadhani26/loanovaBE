package com.example.loanova.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PlafondRequest {
  @NotBlank(message = "Nama plafond wajib diisi")
  @Size(max = 100, message = "Nama plafond maksimal 100 karakter")
  private String name;

  @NotBlank(message = "Deskripsi wajib diisi")
  private String description;

  @NotNull(message = "Jumlah maksimal wajib diisi")
  @DecimalMin(value = "0.0", inclusive = false, message = "Jumlah maksimal harus lebih dari 0")
  private BigDecimal maxAmount;

  @NotNull(message = "Suku bunga wajib diisi")
  @DecimalMin(value = "0.0", inclusive = true, message = "Suku bunga tidak boleh negatif")
  private BigDecimal interestRate;

  @NotNull(message = "Tenor minimal wajib diisi")
  @Min(value = 1, message = "Tenor minimal setidaknya 1 bulan")
  private Integer tenorMin;

  @NotNull(message = "Tenor maksimal wajib diisi")
  @Min(value = 1, message = "Tenor maksimal setidaknya 1 bulan")
  private Integer tenorMax;
}
