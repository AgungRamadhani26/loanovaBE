# Tutorial: Membangun Redis Caching & Security di Spring Boot

Tutorial ini menjelaskan langkah demi langkah cara mengimplementasikan Redis untuk performa (caching) dan keamanan (logout & refresh token) seperti yang ada pada proyek Loanova.

## Langkah 1: Persiapan Environment

### 1. Tambahkan Dependency (pom.xml)
Tambahkan starter data redis untuk integrasi Spring dengan Redis.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. Konfigurasi Redis (application.properties)
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6380
spring.cache.type=redis
```

---

## Langkah 2: Konfigurasi Redis (Java Config)

Buat file `RedisConfig.java` untuk mengatur bagaimana data disimpan (JSON) dan berapa lama data tersebut bertahan (TTL).

**Mengapa ini penting?** 
Agar data di Redis bisa dibaca manusia (JSON) dan kita bisa mengatur TTL yang berbeda untuk setiap jenis data.

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // Konfigurasi Default (1 Jam)
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Konfigurasi Spesifik (misal: User 20 menit)
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("users", config.entryTtl(Duration.ofMinutes(20)));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

---

## Langkah 3: Implementasi Caching (Performa)

Gunakan anotasi Spring Cache pada Service Anda.

1. **`@Cacheable`**: Simpan hasil ke Redis.
   ```java
   @Cacheable(value = "user", key = "#id")
   public UserResponse getUserById(Long id) { ... }
   ```

2. **`@CacheEvict`**: Hapus cache saat data berubah.
   ```java
   @CacheEvict(value = {"user", "users"}, allEntries = true)
   public UserResponse updateUser(Long id, ...) { ... }
   ```

---

## Langkah 4: Keamanan - Refresh Token Rotation

Ubah logika `refreshAccessToken` agar memberikan token baru dan mematikan token lama. Ini mencegah token dicuri dan dipakai berulang kali.

```java
// Di AuthService.java
public AuthResponse refreshAccessToken(String oldRefreshToken) {
    // 1. Validasi token lama
    // 2. Generate Access Token BARU
    // 3. Generate Refresh Token BARU
    // 4. Update data di Database (Ganti token lama dengan yang baru)
}
```

---

## Langkah 5: Keamanan - Logout & Redis Blacklist

### 1. Simpan ke Blacklist (JwtService.java)
Gunakan `StringRedisTemplate` untuk memasukkan token ke Redis secara manual saat user klik logout.

```java
public void blacklistToken(String token) {
    long remainingTime = getSisaWaktuToken(token);
    redisTemplate.opsForValue().set("jwt_blacklist:" + token, "true", remainingTime, TimeUnit.SECONDS);
}
```

### 2. Cek di Security Filter (JwtAuthenticationFilter.java)
Cek setiap request yang masuk. Jika token ada di blacklist, tolak!

```java
if (jwtService.isTokenBlacklisted(token)) {
    filterChain.doFilter(request, response);
    return; // Stop di sini
}
```

---

## Langkah 6: Endpoint Logout (AuthController.java)

Buat pintu keluar bagi user.
```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String token, @RequestBody RefreshTokenRequest req) {
    authService.logout(token.substring(7), req.getRefreshToken());
    return ResponseEntity.ok("Logout Berhasil");
}
```

---
### Kesimpulan
Dengan mengikuti langkah ini, Anda memiliki aplikasi yang:
1. **Cepat**: Data yang jarang berubah diambil dari memori (RAM).
2. **Aman**: User bisa logout total, dan token lama tidak bisa dipalsukan.
