# Panduan Implementasi Redis: Caching & Security (Loanova)

Dokumen ini menjelaskan arsitektur dan cara kerja integrasi Redis yang telah diimplementasikan untuk meningkatkan performa dan keamanan aplikasi.

## 1. Arsitektur Redis
Redis dijalankan menggunakan Docker dengan konfigurasi sebagai berikut:
- **Host**: `localhost`
- **Port**: `6380`
- **Dashboard**: RedisInsight di `http://localhost:5541`

## 2. Fitur Caching (Performa)
Menggunakan Spring Cache Abstraction untuk mengurangi beban database pada query yang sering dipanggil.

### Konfigurasi TTL (Time-To-Live)
Pengaturan durasi cache dilakukan di `RedisConfig.java`:
- **Default**: 1 Jam
- **User Data (`user`, `users`)**: 20 Menit
- **Branch Data (`branch`, `branches`)**: 10 Menit

### Mekanisme Update
- **`@Cacheable`**: Menyimpan hasil query ke Redis.
- **`@CacheEvict`**: Menghapus cache saat terjadi perubahan data (Create/Update/Delete) untuk menjaga konsistensi data.

## 3. Fitur Keamanan (Security)

### A. Refresh Token Rotation
Mekanisme keamanan untuk mencegah pencurian token:
1. Saat user melakukan refresh, sistem akan menghapus Refresh Token lama.
2. User diberikan pasangan **Access Token & Refresh Token BARU**.
3. Jika token lama mencoba digunakan kembali, sistem akan menolak karena sudah tidak ada di Database.

### B. Logout & Blacklist Token (Redis)
Karena JWT bersifat *stateless*, sistem menggunakan Redis untuk mematikan token yang masih berlaku saat user logout.

**Alur Logout:**
1. **Refresh Token**: Dihapus dari Database (Tabel `refresh_tokens`).
2. **Access Token**: Dimasukkan ke dalam **Blacklist Redis** dengan prefix `jwt_blacklist:`.
3. **TTL Dinamis**: Token di-blacklist hanya selama sisa waktu kadaluarsanya (efisien memori).

**Validasi Filter:**
Setiap request yang masuk akan melewati `JwtAuthenticationFilter` yang akan mengecek apakah token tersebut ada di Blacklist Redis sebelum mengizinkan akses ke Controller.

## 4. Cara Pengujian (Testing)
1. **Cek Cache**: Panggil API GET User, cek key di RedisInsight.
2. **Cek Logout**: 
   - Panggil endpoint `POST /api/auth/logout`.
   - Cek key `jwt_blacklist:...` di RedisInsight.
   - Coba gunakan token tersebut untuk akses API lain -> Akan menerima `401 Unauthorized`.

---
*Dokumentasi ini dibuat otomatis sebagai panduan teknis implementasi Redis di proyek Loanova.*
