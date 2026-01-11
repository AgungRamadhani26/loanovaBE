# Implementasi Fitur User Profile - Penjelasan Detail Per Baris

## Daftar Isi

1. [Entity: UserProfile](#1-entity-userprofile)
2. [Repository: UserProfileRepository](#2-repository-userprofilerepository)
3. [DTO Request: UserProfileCompleteRequest](#3-dto-request-userprofilecompleterequest)
4. [DTO Request: UserProfileUpdateRequest](#4-dto-request-userprofileupdaterequest)
5. [DTO Response: UserProfileResponse](#5-dto-response-userprofileresponse)
6. [Service: UserProfileService](#6-service-userprofileservice)
7. [Controller: UserProfileController](#7-controller-userprofilecontroller)

---

## 1. Entity: UserProfile

File: `src/main/java/com/example/loanova/entity/UserProfile.java`

```java
package com.example.loanova.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
```

**Baris 1-6:** Import library yang dibutuhkan

- `jakarta.persistence.*` - Annotation JPA untuk mapping database
- `java.time.*` - Untuk tipe data tanggal dan waktu
- `lombok.*` - Untuk mengurangi boilerplate code

```java
/**
 * USER PROFILE ENTITY Represents user profile information including personal data and document
 * photos. This entity doesn't use soft delete since user profiles should never be deleted, only
 * updated for data accuracy and compliance purposes.
 */
```

**Baris 8-12:** Javadoc yang menjelaskan tujuan entity ini menyimpan informasi profil user dan tidak menggunakan soft delete

```java
@Entity
@Table(name = "users_profiles")
```

**Baris 13-14:**

- `@Entity` - Menandakan ini adalah entity JPA
- `@Table(name = "users_profiles")` - Nama tabel di database adalah "users_profiles"

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
```

**Baris 15-18:** Lombok annotations

- `@Data` - Generate getter, setter, toString, equals, hashCode
- `@Builder` - Generate builder pattern untuk membuat object
- `@NoArgsConstructor` - Generate constructor tanpa parameter
- `@AllArgsConstructor` - Generate constructor dengan semua parameter

```java
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
```

**Baris 19-23:**

- Deklarasi class UserProfile
- `@Id` - Menandai field sebagai primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Auto increment ID
- `private Long id` - Primary key dengan tipe Long

```java
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
```

**Baris 25-27:** Relasi One-to-One dengan entity User

- `@OneToOne` - Satu user hanya punya satu profil
- `fetch = FetchType.EAGER` - Data user langsung di-load saat query profil
- `@JoinColumn(name = "user_id", nullable = false)` - Foreign key ke tabel users dengan nama kolom "user_id", wajib diisi
- `private User user` - Referensi ke entity User

```java
  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;
```

**Baris 29-30:** Field nama lengkap

- `@Column(name = "full_name")` - Nama kolom di database
- `nullable = false` - Wajib diisi (NOT NULL)
- `length = 100` - Maksimal 100 karakter
- `private String fullName` - Variable untuk menyimpan nama lengkap

```java
  @Column(name = "phone_number", nullable = false, unique = true, length = 20)
  private String phoneNumber;
```

**Baris 32-33:** Field nomor telepon

- `unique = true` - Nilai harus unik (tidak boleh duplikat)
- `nullable = false` - Wajib diisi
- `length = 20` - Maksimal 20 karakter

```java
  @Column(name = "user_address", nullable = false)
  private String userAddress;
```

**Baris 35-36:** Field alamat user

- Tidak ada batasan length, bisa panjang
- `nullable = false` - Wajib diisi

```java
  @Column(name = "nik", nullable = false, unique = true, length = 16)
  private String nik;
```

**Baris 38-39:** Field NIK (Nomor Induk Kependudukan)

- `unique = true` - NIK harus unik
- `length = 16` - Panjang NIK tepat 16 karakter
- `nullable = false` - Wajib diisi

```java
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;
```

**Baris 41-42:** Field tanggal lahir

- Tipe `LocalDate` - Hanya tanggal, tanpa jam
- `nullable = false` - Wajib diisi

```java
  @Column(name = "npwp_number", nullable = false, unique = true, length = 16)
  private String npwpNumber;
```

**Baris 44-45:** Field nomor NPWP

- `unique = true` - NPWP harus unik
- `length = 16` - Maksimal 16 karakter
- `nullable = false` - Wajib diisi

```java
  @Column(name = "ktp_photo", nullable = false, length = 255)
  private String ktpPhoto;
```

**Baris 47-48:** Field path/nama file foto KTP

- Menyimpan path file, bukan file itu sendiri
- `length = 255` - Path maksimal 255 karakter
- `nullable = false` - Wajib ada

```java
  @Column(name = "profile_photo", nullable = false, length = 255)
  private String profilePhoto;
```

**Baris 50-51:** Field path foto profil

- Menyimpan lokasi file foto profil user

```java
  @Column(name = "npwp_photo", nullable = false, length = 255)
  private String npwpPhoto;
```

**Baris 53-54:** Field path foto NPWP

- Menyimpan lokasi file foto dokumen NPWP

```java
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
```

**Baris 56-57:** Timestamp kapan profil dibuat

- `updatable = false` - Tidak bisa diupdate setelah dibuat
- Tipe `LocalDateTime` - Tanggal dan waktu lengkap

```java
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
```

**Baris 59-60:** Timestamp kapan profil terakhir diupdate

```java
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
```

**Baris 62-65:** Lifecycle callback sebelum data disimpan pertama kali

- `@PrePersist` - Dijalankan otomatis sebelum insert
- Set `createdAt` dengan waktu sekarang

```java
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
```

**Baris 67-70:** Lifecycle callback sebelum data diupdate

- `@PreUpdate` - Dijalankan otomatis sebelum update
- Set `updatedAt` dengan waktu sekarang

---

## 2. Repository: UserProfileRepository

File: `src/main/java/com/example/loanova/repository/UserProfileRepository.java`

```java
package com.example.loanova.repository;

import com.example.loanova.entity.User;
import com.example.loanova.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
```

**Baris 1-7:** Import dependencies

- Import entity User dan UserProfile
- `Optional` - Untuk menangani kemungkinan data tidak ditemukan
- `JpaRepository` - Interface Spring Data JPA untuk operasi CRUD
- `@Repository` - Annotation Spring untuk repository layer

```java
/** USER PROFILE REPOSITORY - Interface untuk operasi database pada entity UserProfile. */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
```

**Baris 9-11:** Deklarasi repository interface

- `@Repository` - Menandakan ini adalah bean repository Spring
- Extends `JpaRepository<UserProfile, Long>` - Mewarisi method CRUD standar seperti save(), findById(), findAll(), delete(), dll
- `<UserProfile, Long>` - Entity type dan ID type

```java
  /**
   * Mencari profil berdasarkan entity User. Karena hubungan OneToOne, satu user hanya memiliki satu
   * profil.
   *
   * @param user Entity user
   * @return Optional UserProfile
   */
  Optional<UserProfile> findByUser(User user);
```

**Baris 13-20:** Custom query method untuk mencari profil berdasarkan User

- Spring Data JPA akan auto-generate query: `SELECT * FROM users_profiles WHERE user_id = ?`
- Return `Optional<UserProfile>` - Bisa berisi data atau kosong
- Method ini digunakan untuk cek apakah user sudah punya profil

```java
  /** Cek apakah NIK sudah digunakan. */
  boolean existsByNik(String nik);

  Optional<UserProfile> findByNik(String nik);
```

**Baris 22-25:** Method untuk validasi dan pencarian berdasarkan NIK

- `existsByNik()` - Return boolean, true jika NIK sudah ada
- `findByNik()` - Mencari profil dengan NIK tertentu
- Digunakan untuk validasi keunikan NIK

```java
  /** Cek apakah Nomor Telepon sudah digunakan. */
  boolean existsByPhoneNumber(String phoneNumber);

  Optional<UserProfile> findByPhoneNumber(String phoneNumber);
```

**Baris 27-30:** Method untuk validasi dan pencarian berdasarkan nomor telepon

- `existsByPhoneNumber()` - Cek apakah nomor sudah dipakai
- `findByPhoneNumber()` - Cari profil dengan nomor tertentu

```java
  /** Cek apakah NPWP sudah digunakan. */
  boolean existsByNpwpNumber(String npwpNumber);

  Optional<UserProfile> findByNpwpNumber(String npwpNumber);
}
```

**Baris 32-36:** Method untuk validasi dan pencarian berdasarkan NPWP

- `existsByNpwpNumber()` - Cek keberadaan NPWP
- `findByNpwpNumber()` - Cari profil dengan NPWP tertentu
- Semua method ini auto-generated oleh Spring Data JPA

---

## 3. DTO Request: UserProfileCompleteRequest

File: `src/main/java/com/example/loanova/dto/request/UserProfileCompleteRequest.java`

```java
package com.example.loanova.dto.request;

import com.example.loanova.validation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
```

**Baris 1-13:** Import dependencies

- `ValidFile` - Custom validator untuk file upload
- `jakarta.validation.constraints.*` - Annotation validasi standar
- `LocalDate` - Tipe data tanggal
- Lombok annotations
- `DateTimeFormat` - Format parsing tanggal
- `MultipartFile` - Interface untuk handle file upload

```java
/**
 * USER PROFILE COMPLETE REQUEST - DTO khusus untuk pendaftaran/pengisian profil pertama kali. Semua
 * field data pribadi dan foto bersifat WAJIB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCompleteRequest {
```

**Baris 15-23:** Deklarasi class dengan Lombok annotations

- DTO untuk request lengkapi profil (pertama kali)
- Semua field wajib diisi karena ini profil baru
- Lombok generate getter, setter, constructor, builder

```java
  @NotBlank(message = "Nama lengkap wajib diisi")
  @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
  private String fullName;
```

**Baris 25-27:** Field nama lengkap dengan validasi

- `@NotBlank` - Tidak boleh null, empty, atau hanya whitespace
- `@Size(max = 100)` - Maksimal 100 karakter
- Custom error message untuk user-friendly feedback

```java
  @NotBlank(message = "Nomor telepon wajib diisi")
  @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
  private String phoneNumber;
```

**Baris 29-31:** Field nomor telepon dengan validasi

- Wajib diisi dan maksimal 20 karakter

```java
  @NotBlank(message = "Alamat wajib diisi")
  private String userAddress;
```

**Baris 33-34:** Field alamat

- Wajib diisi, tidak ada batasan panjang

```java
  @NotBlank(message = "NIK wajib diisi")
  @Size(min = 16, max = 16, message = "NIK harus 16 karakter")
  private String nik;
```

**Baris 36-38:** Field NIK dengan validasi strict

- `@Size(min = 16, max = 16)` - Harus tepat 16 karakter
- Validasi format NIK Indonesia

```java
  @NotNull(message = "Tanggal lahir wajib diisi")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate birthDate;
```

**Baris 40-42:** Field tanggal lahir

- `@NotNull` - Wajib diisi (untuk object use NotNull, bukan NotBlank)
- `@DateTimeFormat` - Format ISO date (yyyy-MM-dd)

```java
  @NotBlank(message = "Nomor NPWP wajib diisi")
  @Size(max = 16, message = "Nomor NPWP maksimal 16 karakter")
  private String npwpNumber;
```

**Baris 44-46:** Field nomor NPWP

- Wajib diisi, maksimal 16 karakter

```java
  @ValidFile(message = "Foto KTP wajib diunggah", required = true)
  private MultipartFile ktpPhoto;
```

**Baris 48-49:** Field file foto KTP

- `@ValidFile` - Custom validator untuk validasi file (ukuran, format, dll)
- `required = true` - File wajib diunggah
- `MultipartFile` - Object yang menampung file upload

```java
  @ValidFile(message = "Foto profil wajib diunggah", required = true)
  private MultipartFile profilePhoto;
```

**Baris 51-52:** Field file foto profil

- Wajib diunggah saat complete profile

```java
  @ValidFile(message = "Foto NPWP wajib diunggah", required = true)
  private MultipartFile npwpPhoto;
}
```

**Baris 54-56:** Field file foto NPWP

- Wajib diunggah untuk verifikasi dokumen

---

## 4. DTO Request: UserProfileUpdateRequest

File: `src/main/java/com/example/loanova/dto/request/UserProfileUpdateRequest.java`

```java
package com.example.loanova.dto.request;

import com.example.loanova.validation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
```

**Baris 1-13:** Import dependencies (sama dengan CompleteRequest)

```java
/**
 * USER PROFILE UPDATE REQUEST - DTO khusus untuk memperbarui profil. Data pribadi tetap divalidasi,
 * namun foto bersifat OPSIONAL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
```

**Baris 15-23:** Deklarasi class untuk update profil

- PERBEDAAN dengan CompleteRequest: foto-foto bersifat opsional
- User tidak wajib upload ulang foto jika tidak ingin mengubahnya

```java
  @NotBlank(message = "Nama lengkap wajib diisi")
  @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
  private String fullName;

  @NotBlank(message = "Nomor telepon wajib diisi")
  @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
  private String phoneNumber;

  @NotBlank(message = "Alamat wajib diisi")
  private String userAddress;

  @NotBlank(message = "NIK wajib diisi")
  @Size(min = 16, max = 16, message = "NIK harus 16 karakter")
  private String nik;

  @NotNull(message = "Tanggal lahir wajib diisi")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate birthDate;

  @NotBlank(message = "Nomor NPWP wajib diisi")
  @Size(max = 16, message = "Nomor NPWP maksimal 16 karakter")
  private String npwpNumber;
```

**Baris 25-45:** Field data pribadi

- Semua validasi SAMA dengan CompleteRequest
- Data pribadi tetap wajib diisi semua saat update

```java
  // Foto-foto bersifat opsional saat update, tapi tetap divalidasi jika diunggah
  @ValidFile(required = false)
  private MultipartFile ktpPhoto;

  @ValidFile(required = false)
  private MultipartFile profilePhoto;

  @ValidFile(required = false)
  private MultipartFile npwpPhoto;
}
```

**Baris 47-54:** Field foto-foto dengan `required = false`

- `required = false` - Foto tidak wajib diunggah
- Jika user upload foto baru, akan mengganti foto lama
- Jika tidak upload, foto lama tetap digunakan
- Tetap divalidasi (format, ukuran) jika user upload

---

## 5. DTO Response: UserProfileResponse

File: `src/main/java/com/example/loanova/dto/response/UserProfileResponse.java`

```java
package com.example.loanova.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
```

**Baris 1-8:** Import dependencies

- Tipe data tanggal dan waktu
- Lombok annotations

```java
/** USER PROFILE RESPONSE - DTO untuk mengirimkan data profil pengguna ke client. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
```

**Baris 10-15:** Deklarasi class response DTO

- Digunakan untuk mengirim data ke client (frontend)
- Berisi semua data profil yang aman untuk dikirim

```java
  private Long id;
  private Long userId;
  private String username;
```

**Baris 16-18:** Field identitas

- `id` - ID profil
- `userId` - ID user yang punya profil
- `username` - Username dari user

```java
  private String fullName;
  private String phoneNumber;
  private String userAddress;
  private String nik;
  private LocalDate birthDate;
  private String npwpNumber;
```

**Baris 19-24:** Field data pribadi

- Semua data pribadi user yang tersimpan

```java
  private String ktpPhoto;
  private String profilePhoto;
  private String npwpPhoto;
```

**Baris 25-27:** Field path foto

- Berisi path/URL ke file foto
- Frontend bisa menggunakan path ini untuk menampilkan gambar

```java
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

**Baris 28-30:** Field metadata

- `createdAt` - Kapan profil dibuat
- `updatedAt` - Kapan terakhir diupdate

---

## 6. Service: UserProfileService

File: `src/main/java/com/example/loanova/service/UserProfileService.java`

```java
package com.example.loanova.service;

import com.example.loanova.dto.request.UserProfileCompleteRequest;
import com.example.loanova.dto.request.UserProfileUpdateRequest;
import com.example.loanova.dto.response.UserProfileResponse;
import com.example.loanova.entity.User;
import com.example.loanova.entity.UserProfile;
import com.example.loanova.exception.BusinessException;
import com.example.loanova.exception.ResourceNotFoundException;
import com.example.loanova.repository.UserProfileRepository;
import com.example.loanova.repository.UserRepository;
import com.example.loanova.util.FileStorageUtil;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

**Baris 1-16:** Import dependencies

- DTO request dan response
- Entity User dan UserProfile
- Custom exception classes
- Repository interfaces
- FileStorageUtil untuk handle upload file
- `IOException` untuk handle error file operations
- `@Transactional` untuk transaction management

```java
/** USER PROFILE SERVICE - Menangani logika bisnis untuk profil pengguna. */
@Service
@RequiredArgsConstructor
public class UserProfileService {
```

**Baris 18-21:** Deklarasi service class

- `@Service` - Menandakan ini adalah service layer Spring
- `@RequiredArgsConstructor` - Lombok generate constructor untuk final fields (dependency injection)

```java
  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FileStorageUtil fileStorageUtil;
```

**Baris 23-25:** Dependency injection

- `userProfileRepository` - Repository untuk CRUD profil
- `userRepository` - Repository untuk query user
- `fileStorageUtil` - Utility untuk simpan/hapus file

### Method 1: completeProfile()

```java
  /** LENGKAPI PROFIL - Untuk pengguna role CUSTOMER yang baru mendaftar. */
  @Transactional
  public UserProfileResponse completeProfile(String username, UserProfileCompleteRequest request) {
```

**Baris 27-29:** Deklarasi method lengkapi profil

- `@Transactional` - Semua operasi dalam satu transaction, rollback jika ada error
- Input: username (dari authentication) dan request DTO
- Return: UserProfileResponse

```java
    // 1. Ambil data User yang sedang login
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
```

**Baris 30-34:** Cari user berdasarkan username

- Query ke database mencari user
- `.orElseThrow()` - Jika tidak ketemu, throw exception ResourceNotFoundException

```java
    // 2. Pastikan user belum memiliki profil
    if (userProfileRepository.findByUser(user).isPresent()) {
      throw new BusinessException(
          "Profil sudah dilengkapi. Gunakan fitur update untuk mengubah data.");
    }
```

**Baris 36-40:** Validasi user belum punya profil

- Cek apakah user sudah punya profil
- `.isPresent()` - Return true jika ada data
- Jika sudah ada, throw BusinessException dengan pesan error

```java
    // 3. Validasi keunikan data
    validateUniqueness(request.getNik(), request.getPhoneNumber(), request.getNpwpNumber(), null);
```

**Baris 42-43:** Validasi keunikan NIK, phone, NPWP

- Panggil method `validateUniqueness()` (dijelaskan di bawah)
- Parameter terakhir `null` karena ini profil baru (belum ada ID)

```java
    try {
      // 4. Simpan file-file foto
      String ktpPath = fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp");
      String profilePath = fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles");
      String npwpPath = fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp");
```

**Baris 45-49:** Upload dan simpan file foto

- `saveFile()` - Method dari FileStorageUtil untuk save file ke disk
- Parameter 1: MultipartFile (file yang diupload)
- Parameter 2: folder tujuan (ktp/, profiles/, npwp/)
- Return: path lengkap file yang tersimpan

```java
      // 5. Buat entity UserProfile
      UserProfile userProfile =
          UserProfile.builder()
              .user(user)
              .fullName(request.getFullName())
              .phoneNumber(request.getPhoneNumber())
              .userAddress(request.getUserAddress())
              .nik(request.getNik())
              .birthDate(request.getBirthDate())
              .npwpNumber(request.getNpwpNumber())
              .ktpPhoto(ktpPath)
              .profilePhoto(profilePath)
              .npwpPhoto(npwpPath)
              .build();
```

**Baris 51-64:** Buat object UserProfile menggunakan builder pattern

- Set semua field dari request DTO
- Set path file yang sudah disimpan
- `.build()` - Create object UserProfile

```java
      UserProfile savedProfile = userProfileRepository.save(userProfile);
      return toResponse(savedProfile);
```

**Baris 66-67:** Simpan ke database dan return response

- `save()` - Simpan entity ke database
- `toResponse()` - Convert entity ke response DTO

```java
    } catch (IOException e) {
      throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
    }
  }
```

**Baris 69-72:** Handle error file operations

- Jika terjadi IOException (gagal simpan file)
- Throw BusinessException dengan pesan error
- Transaction akan di-rollback otomatis

### Method 2: updateProfile()

```java
  /** UPDATE PROFIL - Untuk memperbarui data profil yang ada. */
  @Transactional
  public UserProfileResponse updateProfile(String username, UserProfileUpdateRequest request) {
```

**Baris 74-76:** Deklarasi method update profil

- `@Transactional` - Wrapping dalam transaction
- Input: username dan UserProfileUpdateRequest

```java
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
```

**Baris 77-81:** Cari user yang login

```java
    UserProfile userProfile =
        userProfileRepository
            .findByUser(user)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Profil belum dilengkapi. Silakan lengkapi profil terlebih dahulu."));
```

**Baris 83-89:** Cari profil user yang sudah ada

- Jika belum ada profil, throw exception
- User harus complete profile dulu sebelum bisa update

```java
    // Validasi keunikan data (kecuali data milik user sendiri)
    validateUniqueness(
        request.getNik(), request.getPhoneNumber(), request.getNpwpNumber(), userProfile.getId());
```

**Baris 91-93:** Validasi keunikan dengan mengecualikan data user sendiri

- Parameter terakhir: `userProfile.getId()` - ID profil yang sedang diupdate
- Validasi akan skip jika data yang duplikat adalah milik user ini sendiri

```java
    // Update data dasar
    userProfile.setFullName(request.getFullName());
    userProfile.setPhoneNumber(request.getPhoneNumber());
    userProfile.setUserAddress(request.getUserAddress());
    userProfile.setNik(request.getNik());
    userProfile.setBirthDate(request.getBirthDate());
    userProfile.setNpwpNumber(request.getNpwpNumber());
```

**Baris 95-101:** Update data pribadi

- Set semua field data pribadi dengan nilai baru dari request

```java
    try {
      // Update foto jika ada yang diunggah baru (opsional)
      if (request.getKtpPhoto() != null && !request.getKtpPhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getKtpPhoto());
        userProfile.setKtpPhoto(fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp"));
      }
```

**Baris 103-109:** Update foto KTP jika ada file baru

- Cek apakah user upload foto baru: `!= null && !isEmpty()`
- Hapus file lama dari disk: `deleteFile()`
- Simpan file baru dan update path di entity

```java
      if (request.getProfilePhoto() != null && !request.getProfilePhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getProfilePhoto());
        userProfile.setProfilePhoto(
            fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles"));
      }
```

**Baris 110-115:** Update foto profil jika ada file baru

- Logika sama dengan foto KTP

```java
      if (request.getNpwpPhoto() != null && !request.getNpwpPhoto().isEmpty()) {
        // Hapus file lama sebelum save file baru
        fileStorageUtil.deleteFile(userProfile.getNpwpPhoto());
        userProfile.setNpwpPhoto(fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp"));
      }
```

**Baris 116-120:** Update foto NPWP jika ada file baru

- Logika sama dengan foto-foto lainnya

```java
      UserProfile updatedProfile = userProfileRepository.save(userProfile);
      return toResponse(updatedProfile);
```

**Baris 122-123:** Simpan perubahan ke database

- `save()` - Update record di database
- Return response DTO

```java
    } catch (IOException e) {
      throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
    }
  }
```

**Baris 125-128:** Handle error file operations

### Method 3: getMyProfile()

```java
  /** AMBIL PROFIL SAYA - Mendapatkan data profil user yang sedang login. */
  @Transactional(readOnly = true)
  public UserProfileResponse getMyProfile(String username) {
```

**Baris 130-132:** Method untuk get profil sendiri

- `@Transactional(readOnly = true)` - Read-only transaction (optimasi performa)
- Input: username dari authentication

```java
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
```

**Baris 133-137:** Cari user berdasarkan username

```java
    UserProfile userProfile =
        userProfileRepository
            .findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Profil belum dilengkapi"));
```

**Baris 139-143:** Cari profil user

- Throw exception jika profil belum ada

```java
    return toResponse(userProfile);
  }
```

**Baris 145-146:** Return response DTO

### Method 4: validateUniqueness()

```java
  /** Validasi keunikan NIK, Phone, dan NPWP. */
  private void validateUniqueness(
      String nik, String phoneNumber, String npwpNumber, Long currentProfileId) {
```

**Baris 148-150:** Private method untuk validasi keunikan data

- Parameter: nik, phoneNumber, npwpNumber yang akan divalidasi
- `currentProfileId` - ID profil saat ini (null jika profil baru)

```java
    // Check NIK
    userProfileRepository
        .findByNik(nik)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("NIK sudah digunakan oleh pengguna lain");
              }
            });
```

**Baris 151-159:** Validasi keunikan NIK

- `findByNik()` - Cari profil dengan NIK tersebut
- `.ifPresent()` - Jika ditemukan, jalankan logic di dalam lambda
- Cek: apakah NIK duplikat dari user lain? (bukan milik user ini sendiri)
- `currentProfileId == null` - Profil baru, pasti tidak boleh duplikat
- `!existing.getId().equals(currentProfileId)` - Duplikat dari user lain
- Jika duplikat dari user lain, throw BusinessException

```java
    // Check Phone
    userProfileRepository
        .findByPhoneNumber(phoneNumber)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor telepon sudah digunakan oleh pengguna lain");
              }
            });
```

**Baris 161-169:** Validasi keunikan nomor telepon

- Logika sama dengan validasi NIK

```java
    // Check NPWP
    userProfileRepository
        .findByNpwpNumber(npwpNumber)
        .ifPresent(
            existing -> {
              if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor NPWP sudah digunakan oleh pengguna lain");
              }
            });
  }
```

**Baris 171-180:** Validasi keunikan NPWP

- Logika sama dengan validasi NIK dan phone

### Method 5: toResponse()

```java
  /** Mapper Entity to Response DTO. */
  private UserProfileResponse toResponse(UserProfile profile) {
    return UserProfileResponse.builder()
        .id(profile.getId())
        .userId(profile.getUser().getId())
        .username(profile.getUser().getUsername())
        .fullName(profile.getFullName())
        .phoneNumber(profile.getPhoneNumber())
        .userAddress(profile.getUserAddress())
        .nik(profile.getNik())
        .birthDate(profile.getBirthDate())
        .npwpNumber(profile.getNpwpNumber())
        .ktpPhoto(profile.getKtpPhoto())
        .profilePhoto(profile.getProfilePhoto())
        .npwpPhoto(profile.getNpwpPhoto())
        .createdAt(profile.getCreatedAt())
        .updatedAt(profile.getUpdatedAt())
        .build();
  }
}
```

**Baris 182-200:** Private method untuk mapping entity ke DTO

- Convert UserProfile entity menjadi UserProfileResponse DTO
- Copy semua field yang diperlukan
- Mengambil data dari relasi: `profile.getUser().getId()`
- Return object response yang siap dikirim ke client

---

## 7. Controller: UserProfileController

File: `src/main/java/com/example/loanova/controller/UserProfileController.java`

```java
package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.UserProfileCompleteRequest;
import com.example.loanova.dto.request.UserProfileUpdateRequest;
import com.example.loanova.dto.response.UserProfileResponse;
import com.example.loanova.service.UserProfileService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
```

**Baris 1-15:** Import dependencies

- `ApiResponse` - Wrapper response standar
- DTO request dan response
- `UserProfileService` - Business logic layer
- `ResponseUtil` - Utility untuk create response
- `@Valid` - Trigger validasi DTO
- `MediaType` - Untuk multipart/form-data
- `ResponseEntity` - Wrapper HTTP response Spring
- `@PreAuthorize` - Security authorization
- `Authentication` - Object untuk get user yang login
- REST controller annotations

```java
/** USER PROFILE CONTROLLER - Endpoint untuk mengelola profil pengguna (khusus CUSTOMER). */
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {
```

**Baris 17-21:** Deklarasi controller class

- `@RestController` - Gabungan @Controller dan @ResponseBody
- `@RequestMapping("/api/user-profiles")` - Base path untuk semua endpoint
- `@RequiredArgsConstructor` - Lombok DI untuk final fields

```java
  private final UserProfileService userProfileService;
```

**Baris 23:** Dependency injection UserProfileService

### Endpoint 1: Complete Profile (POST /api/user-profiles/complete)

```java
  /** LENGKAPI PROFIL Khusus role CUSTOMER. Menggunakan multipart/form-data untuk unggahn file. */
  @PreAuthorize("hasRole('CUSTOMER')")
  @PostMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<UserProfileResponse>> completeProfile(
      @Valid @ModelAttribute UserProfileCompleteRequest request, Authentication authentication) {
```

**Baris 25-29:** Endpoint untuk lengkapi profil

- `@PreAuthorize("hasRole('CUSTOMER')")` - Hanya user dengan role CUSTOMER yang bisa akses
- `@PostMapping("/complete")` - HTTP POST ke /api/user-profiles/complete
- `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` - Accept multipart/form-data (untuk upload file)
- `@Valid` - Trigger validasi pada request DTO
- `@ModelAttribute` - Binding request multipart ke object (bukan @RequestBody karena ada file)
- `Authentication authentication` - Object berisi info user yang login

```java
    UserProfileResponse response =
        userProfileService.completeProfile(authentication.getName(), request);
    return ResponseUtil.created(response, "Profil berhasil dilengkapi");
  }
```

**Baris 30-33:** Proses request

- `authentication.getName()` - Get username dari user yang login
- Panggil service method `completeProfile()`
- `ResponseUtil.created()` - Generate HTTP 201 Created response
- Return response dengan status 201 dan message

### Endpoint 2: Update Profile (PUT /api/user-profiles/update)

```java
  /** UPDATE PROFIL Memperbarui data profil yang ada. */
  @PreAuthorize("hasRole('CUSTOMER')")
  @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
      @Valid @ModelAttribute UserProfileUpdateRequest request, Authentication authentication) {
```

**Baris 35-39:** Endpoint untuk update profil

- `@PutMapping("/update")` - HTTP PUT ke /api/user-profiles/update
- Sama seperti complete, tapi menggunakan UserProfileUpdateRequest

```java
    UserProfileResponse response =
        userProfileService.updateProfile(authentication.getName(), request);
    return ResponseUtil.ok(response, "Profil berhasil diperbarui");
  }
```

**Baris 40-43:** Proses request

- Panggil service method `updateProfile()`
- `ResponseUtil.ok()` - Generate HTTP 200 OK response
- Return response dengan status 200

### Endpoint 3: Get My Profile (GET /api/user-profiles/me)

```java
  /** AMBIL PROFIL SAYA */
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
      Authentication authentication) {
```

**Baris 45-49:** Endpoint untuk get profil sendiri

- `@GetMapping("/me")` - HTTP GET ke /api/user-profiles/me
- Tidak ada request body, hanya authentication

```java
    UserProfileResponse response = userProfileService.getMyProfile(authentication.getName());
    return ResponseUtil.ok(response, "Berhasil mengambil data profil");
  }
}
```

**Baris 50-53:** Proses request

- Panggil service method `getMyProfile()`
- Return response dengan data profil user

---

## Ringkasan Alur Kerja Fitur User Profile

### 1. Complete Profile Flow:

1. User (CUSTOMER) kirim POST request ke `/api/user-profiles/complete`
2. Controller validate role (harus CUSTOMER)
3. Controller validate request DTO (semua field wajib diisi)
4. Controller forward ke Service layer
5. Service cari data User berdasarkan username
6. Service validasi user belum punya profil
7. Service validasi keunikan NIK, phone, NPWP
8. Service upload dan simpan file foto ke disk
9. Service buat entity UserProfile dan simpan ke database
10. Service convert entity ke response DTO
11. Controller return HTTP 201 Created dengan data profil

### 2. Update Profile Flow:

1. User (CUSTOMER) kirim PUT request ke `/api/user-profiles/update`
2. Controller validate role dan request DTO
3. Service cari User dan UserProfile yang sudah ada
4. Service validasi keunikan data (kecuali milik user sendiri)
5. Service update data pribadi
6. Service cek dan update foto jika ada file baru (hapus file lama)
7. Service simpan perubahan ke database
8. Controller return HTTP 200 OK dengan data profil terupdate

### 3. Get My Profile Flow:

1. User (CUSTOMER) kirim GET request ke `/api/user-profiles/me`
2. Controller validate role
3. Service cari User dan UserProfile
4. Service convert entity ke response DTO
5. Controller return HTTP 200 OK dengan data profil

## Keamanan & Best Practices:

- ✅ Authorization: Hanya CUSTOMER yang bisa akses
- ✅ Authentication: Menggunakan username dari token JWT
- ✅ Validation: Semua input divalidasi (data pribadi dan file)
- ✅ Transaction Management: Menggunakan @Transactional
- ✅ Error Handling: Custom exception dengan message yang jelas
- ✅ File Management: Upload, update, dan delete file dengan proper cleanup
- ✅ Data Integrity: Validasi keunikan NIK, phone, NPWP
- ✅ Separation of Concerns: Entity, DTO, Repository, Service, Controller terpisah

## Testing Endpoints dengan Postman/cURL:

### 1. Complete Profile:

```bash
POST /api/user-profiles/complete
Content-Type: multipart/form-data
Authorization: Bearer <token>

Form Data:
- fullName: "John Doe"
- phoneNumber: "081234567890"
- userAddress: "Jl. Example No. 123"
- nik: "1234567890123456"
- birthDate: "1990-01-01"
- npwpNumber: "1234567890123456"
- ktpPhoto: [file]
- profilePhoto: [file]
- npwpPhoto: [file]
```

### 2. Update Profile:

```bash
PUT /api/user-profiles/update
Content-Type: multipart/form-data
Authorization: Bearer <token>

Form Data:
- fullName: "John Doe Updated"
- phoneNumber: "081234567891"
- userAddress: "Jl. New Address"
- nik: "1234567890123456"
- birthDate: "1990-01-01"
- npwpNumber: "1234567890123456"
- ktpPhoto: [file] (optional)
- profilePhoto: [file] (optional)
- npwpPhoto: [file] (optional)
```

### 3. Get My Profile:

```bash
GET /api/user-profiles/me
Authorization: Bearer <token>
```

---

**Dokumentasi dibuat pada:** 10 Januari 2026
**Project:** LoanOva - Loan Management System
**Feature:** User Profile Management
