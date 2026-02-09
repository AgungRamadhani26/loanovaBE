package com.example.loanova;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration Test untuk memastikan ApplicationContext dapat dimuat dengan benar.
 *
 * <p>CATATAN: Test ini di-disable untuk CI karena membutuhkan:
 *
 * <ul>
 *   <li>Database (SQL Server)
 *   <li>Redis
 *   <li>Konfigurasi environment yang lengkap
 * </ul>
 *
 * <p>Jalankan test ini secara manual di lokal dengan database yang tersedia.
 */
@SpringBootTest
@Disabled("Disabled untuk CI - membutuhkan database dan Redis")
class LoanovaApplicationTests {

  @Test
  void contextLoads() {}
}
