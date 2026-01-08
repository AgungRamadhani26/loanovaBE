# 📘 TUTORIAL USER PROFILE MANAGEMENT - LOANOVA

## 📑 Daftar Isi

1. [Pengantar & Arsitektur](#pengantar--arsitektur)
2. [Entity Layer](#entity-layer)
3. [DTO Layer](#dto-layer)
4. [Repository Layer](#repository-layer)
5. [Validation Layer](#validation-layer)
6. [Service Layer](#service-layer)
7. [Controller Layer](#controller-layer)
8. [Configuration Layer](#configuration-layer)
9. [Flow Diagram](#flow-diagram)
10. [Best Practices](#best-practices)

---

## 1. Pengantar & Arsitektur

### 📋 Fitur User Profile

Fitur ini memungkinkan **CUSTOMER** untuk:

- ✅ Melengkapi profil pertama kali (wajib)
- ✅ Mengupdate profil yang sudah ada
- ✅ Upload foto KTP, Profil, dan NPWP
- ✅ Validasi data pribadi dan file
- ✅ Melihat profil sendiri

### 🏗️ Arsitektur Layer

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Postman/Frontend)             │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP Request (multipart/form-data)
                         ▼
┌─────────────────────────────────────────────────────────┐
│              CONTROLLER LAYER (UserProfileController)    │
│  - Handle HTTP Request                                   │
│  - Validasi Input (@Valid)                               │
│  - Return Response                                       │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              SERVICE LAYER (UserProfileService)          │
│  - Business Logic                                        │
│  - Validasi Keunikan Data (NIK, Phone, NPWP)           │
│  - Koordinasi File Upload                                │
│  - Transaction Management                                │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
┌─────────────┐  ┌──────────────┐  ┌─────────────┐
│ REPOSITORY  │  │ FILE STORAGE │  │  VALIDATOR  │
│   LAYER     │  │    UTIL      │  │    LAYER    │
└─────────────┘  └──────────────┘  └─────────────┘
         │               │               │
         ▼               ▼               ▼
┌─────────────┐  ┌──────────────┐  ┌─────────────┐
│  DATABASE   │  │ FILE SYSTEM  │  │  RULES      │
└─────────────┘  └──────────────┘  └─────────────┘
```

---

## 2. Entity Layer

### 📄 File: `UserProfile.java`

**Lokasi**: `src/main/java/com/example/loanova/entity/UserProfile.java`

```java
@Entity
@Table(name = "users_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relasi OneToOne dengan User
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(name = "phone_number", nullable = false, unique = true, length = 20)
  private String phoneNumber;

  @Column(name = "user_address", nullable = false)
  private String userAddress;

  @Column(name = "nik", nullable = false, unique = true, length = 16)
  private String nik;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "npwp_number", nullable = false, unique = true, length = 16)
  private String npwpNumber;

  @Column(name = "ktp_photo", nullable = false, length = 255)
  private String ktpPhoto;

  @Column(name = "profile_photo", nullable = false, length = 255)
  private String profilePhoto;

  @Column(name = "npwp_photo", nullable = false, length = 255)
  private String npwpPhoto;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Automatically set timestamps
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
```

### 📝 Penjelasan Entity:

#### **1. Annotations:**

- `@Entity`: Menandai class sebagai JPA entity (table di database)
- `@Table(name = "users_profiles")`: Nama table di database
- `@Data`: Lombok - auto generate getter, setter, toString, equals, hashCode
- `@Builder`: Lombok - builder pattern untuk create object
- `@NoArgsConstructor` & `@AllArgsConstructor`: Constructor tanpa parameter dan dengan semua parameter

#### **2. Primary Key:**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

- `@Id`: Menandai sebagai primary key
- `@GeneratedValue`: Auto-increment

#### **3. Relasi OneToOne dengan User:**

```java
@OneToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

- Satu User hanya punya satu Profile
- `EAGER`: Load profile bersamaan dengan user
- `user_id`: Foreign key di table

#### **4. Unique Constraints:**

```java
@Column(name = "phone_number", nullable = false, unique = true)
@Column(name = "nik", nullable = false, unique = true)
@Column(name = "npwp_number", nullable = false, unique = true)
```

- Database level constraint
- Mencegah duplikasi data

#### **5. Audit Fields:**

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

- `@PrePersist`: Jalan sebelum INSERT
- `@PreUpdate`: Jalan sebelum UPDATE
- Auto set timestamp

---

## 3. DTO Layer

### 📄 File: `UserProfileCompleteRequest.java`

**Lokasi**: `src/main/java/com/example/loanova/dto/request/UserProfileCompleteRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCompleteRequest {

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

    @ValidFile(message = "Foto KTP wajib diunggah", required = true)
    private MultipartFile ktpPhoto;

    @ValidFile(message = "Foto profil wajib diunggah", required = true)
    private MultipartFile profilePhoto;

    @ValidFile(message = "Foto NPWP wajib diunggah", required = true)
    private MultipartFile npwpPhoto;
}
```

### 📝 Penjelasan DTO Request:

#### **1. Mengapa Pakai DTO?**

- ✅ **Separation of Concern**: Request data terpisah dari Entity
- ✅ **Validation**: Validasi di layer DTO, bukan Entity
- ✅ **Flexibility**: Bisa punya multiple DTO untuk use case berbeda
- ✅ **Security**: Tidak expose internal entity structure

#### **2. Validasi Annotations:**

```java
@NotBlank(message = "Nama lengkap wajib diisi")
```

- Validasi otomatis oleh Spring Validation
- Error message kustom

```java
@Size(min = 16, max = 16, message = "NIK harus 16 karakter")
```

- Validasi panjang string
- NIK Indonesia harus 16 digit

```java
@ValidFile(message = "Foto KTP wajib diunggah", required = true)
```

- Custom validator untuk MultipartFile
- Cek file kosong, ukuran, dan tipe

#### **3. MultipartFile:**

```java
private MultipartFile ktpPhoto;
```

- Untuk handle file upload
- Spring Boot auto bind dari form-data

---

### 📄 File: `UserProfileUpdateRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String fullName;

    // ... field lainnya sama dengan CompleteRequest

    // File OPSIONAL saat update
    @ValidFile(required = false)
    private MultipartFile ktpPhoto;

    @ValidFile(required = false)
    private MultipartFile profilePhoto;

    @ValidFile(required = false)
    private MultipartFile npwpPhoto;
}
```

### 📝 Perbedaan Complete vs Update:

| Aspek        | CompleteRequest           | UpdateRequest                   |
| ------------ | ------------------------- | ------------------------------- |
| **Foto**     | Wajib (`required = true`) | Opsional (`required = false`)   |
| **Use Case** | Profil pertama kali       | Update profil existing          |
| **Validasi** | Semua field wajib         | Field text wajib, file opsional |

---

### 📄 File: `UserProfileResponse.java`

**Lokasi**: `src/main/java/com/example/loanova/dto/response/UserProfileResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String userAddress;
    private String nik;
    private LocalDate birthDate;
    private String npwpNumber;
    private String ktpPhoto;        // Path: "ktp/uuid.jpg"
    private String profilePhoto;     // Path: "profiles/uuid.jpg"
    private String npwpPhoto;        // Path: "npwp/uuid.jpg"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 📝 Penjelasan Response DTO:

#### **1. Kenapa Perlu Response DTO?**

- ✅ **Security**: Tidak expose sensitive data (password, token)
- ✅ **Customization**: Bisa tambah/kurangi field sesuai kebutuhan frontend
- ✅ **Documentation**: Clear contract antara backend dan frontend

#### **2. Field ktpPhoto, profilePhoto, npwpPhoto:**

```java
private String ktpPhoto; // "ktp/uuid-xxx.jpg"
```

- Format: `subfolder/filename`
- Frontend bisa construct URL: `http://localhost:9091/uploads/ktp/uuid-xxx.jpg`

---

## 4. Repository Layer

### 📄 File: `UserProfileRepository.java`

**Lokasi**: `src/main/java/com/example/loanova/repository/UserProfileRepository.java`

```java
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Mencari profil berdasarkan User entity
     */
    Optional<UserProfile> findByUser(User user);

    /**
     * Cek keunikan NIK
     */
    boolean existsByNik(String nik);
    Optional<UserProfile> findByNik(String nik);

    /**
     * Cek keunikan Phone Number
     */
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<UserProfile> findByPhoneNumber(String phoneNumber);

    /**
     * Cek keunikan NPWP
     */
    boolean existsByNpwpNumber(String npwpNumber);
    Optional<UserProfile> findByNpwpNumber(String npwpNumber);
}
```

### 📝 Penjelasan Repository:

#### **1. Extends JpaRepository:**

```java
extends JpaRepository<UserProfile, Long>
```

- `UserProfile`: Entity type
- `Long`: Primary key type
- Auto dapat methods: `save()`, `findById()`, `findAll()`, `delete()`, dll

#### **2. Query Methods:**

Spring Data JPA auto generate query berdasarkan nama method:

```java
Optional<UserProfile> findByNik(String nik);
```

Generated SQL: `SELECT * FROM users_profiles WHERE nik = ?`

#### **3. exists Methods:**

```java
boolean existsByNik(String nik);
```

- Lebih efisien untuk cek keberadaan data
- Return true/false

---

## 5. Validation Layer

### 📄 File: `ValidFile.java` (Custom Annotation)

**Lokasi**: `src/main/java/com/example/loanova/validation/ValidFile.java`

```java
@Documented
@Constraint(validatedBy = ValidFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "File wajib diunggah";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Apakah file wajib diisi
     */
    boolean required() default true;

    /**
     * Maksimal ukuran file (bytes)
     * Default: 3MB
     */
    long maxSize() default 3 * 1024 * 1024;

    /**
     * Content type yang diizinkan
     */
    String[] allowedTypes() default {"image/*"};
}
```

### 📝 Penjelasan Custom Annotation:

#### **1. Meta-Annotations:**

```java
@Constraint(validatedBy = ValidFileValidator.class)
```

- Link annotation dengan validator class

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
```

- Bisa dipakai di field dan parameter method

```java
@Retention(RetentionPolicy.RUNTIME)
```

- Annotation aktif saat runtime

#### **2. Custom Parameters:**

```java
boolean required() default true;
long maxSize() default 3 * 1024 * 1024;
String[] allowedTypes() default {"image/*"};
```

- Bisa dikustomisasi per field
- Contoh: `@ValidFile(required = false, maxSize = 5MB)`

---

### 📄 File: `ValidFileValidator.java` (Validator Implementation)

**Lokasi**: `src/main/java/com/example/loanova/validation/ValidFileValidator.java`

```java
public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private boolean required;
    private long maxSize;
    private String[] allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Cek apakah file kosong (berbagai kondisi)
        boolean isFileEmpty = file == null
                || file.isEmpty()
                || file.getSize() == 0
                || file.getOriginalFilename() == null
                || file.getOriginalFilename().trim().isEmpty();

        // Jika file tidak required dan kosong, maka valid
        if (!required && isFileEmpty) {
            return true;
        }

        // Jika file required tetapi kosong, maka invalid
        if (required && isFileEmpty) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File wajib diunggah")
                    .addConstraintViolation();
            return false;
        }

        // Validasi ukuran file
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Ukuran file terlalu besar. Maksimal " + (maxSize / (1024 * 1024)) + "MB")
                    .addConstraintViolation();
            return false;
        }

        // Validasi tipe file
        String contentType = file.getContentType();
        if (contentType == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Tipe file tidak valid")
                    .addConstraintViolation();
            return false;
        }

        boolean isValidType = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.endsWith("/*")) {
                // Wildcard matching (contoh: image/*)
                String prefix = allowedType.substring(0, allowedType.length() - 1);
                if (contentType.startsWith(prefix)) {
                    isValidType = true;
                    break;
                }
            } else if (contentType.equals(allowedType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File harus berupa gambar (JPG/PNG)")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
```

### 📝 Penjelasan Validator Logic:

#### **1. Check File Empty (Comprehensive):**

```java
boolean isFileEmpty = file == null
        || file.isEmpty()
        || file.getSize() == 0
        || file.getOriginalFilename() == null
        || file.getOriginalFilename().trim().isEmpty();
```

**Kenapa banyak kondisi?**

- Spring Boot bisa kirim MultipartFile dalam berbagai state
- Postman kosong → `file.isEmpty() == true`
- Form HTML kosong → `file.getSize() == 0`
- Perlu cek semua kondisi untuk reliabilitas

#### **2. Validasi Size:**

```java
if (file.getSize() > maxSize) {
    // Error message
}
```

- Cek ukuran file dalam bytes
- Default: 3MB (3 _ 1024 _ 1024 bytes)

#### **3. Validasi Content Type:**

```java
String contentType = file.getContentType(); // "image/jpeg", "image/png"
```

- Wildcard matching: `image/*` match semua gambar
- Exact matching: `image/jpeg` hanya JPG

#### **4. Custom Error Message:**

```java
context.disableDefaultConstraintViolation();
context.buildConstraintViolationWithTemplate("Custom message")
        .addConstraintViolation();
```

- Override default message
- Error message dinamis berdasarkan kondisi

---

## 6. Service Layer

### 📄 File: `UserProfileService.java`

**Lokasi**: `src/main/java/com/example/loanova/service/UserProfileService.java`

```java
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;

    /**
     * LENGKAPI PROFIL - Pertama kali
     */
    @Transactional
    public UserProfileResponse completeProfile(String username, UserProfileCompleteRequest request) {
        // 1. Ambil User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // 2. Cek apakah sudah punya profil
        if (userProfileRepository.findByUser(user).isPresent()) {
            throw new BusinessException("Profil sudah dilengkapi. Gunakan fitur update.");
        }

        // 3. Validasi keunikan data
        validateUniqueness(request.getNik(), request.getPhoneNumber(),
                          request.getNpwpNumber(), null);

        try {
            // 4. Simpan file-file
            String ktpPath = fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp");
            String profilePath = fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles");
            String npwpPath = fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp");

            // 5. Buat entity UserProfile
            UserProfile userProfile = UserProfile.builder()
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

            UserProfile savedProfile = userProfileRepository.save(userProfile);
            return toResponse(savedProfile);

        } catch (IOException e) {
            throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
        }
    }

    /**
     * UPDATE PROFIL - Update data yang ada
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profil belum dilengkapi. Silakan lengkapi profil terlebih dahulu."));

        // Validasi keunikan (kecuali data milik user sendiri)
        validateUniqueness(request.getNik(), request.getPhoneNumber(),
                          request.getNpwpNumber(), userProfile.getId());

        // Update data dasar
        userProfile.setFullName(request.getFullName());
        userProfile.setPhoneNumber(request.getPhoneNumber());
        userProfile.setUserAddress(request.getUserAddress());
        userProfile.setNik(request.getNik());
        userProfile.setBirthDate(request.getBirthDate());
        userProfile.setNpwpNumber(request.getNpwpNumber());

        try {
            // Update foto HANYA jika ada upload baru
            if (request.getKtpPhoto() != null && !request.getKtpPhoto().isEmpty()) {
                userProfile.setKtpPhoto(fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp"));
            }
            if (request.getProfilePhoto() != null && !request.getProfilePhoto().isEmpty()) {
                userProfile.setProfilePhoto(fileStorageUtil.saveFile(request.getProfilePhoto(), "profiles"));
            }
            if (request.getNpwpPhoto() != null && !request.getNpwpPhoto().isEmpty()) {
                userProfile.setNpwpPhoto(fileStorageUtil.saveFile(request.getNpwpPhoto(), "npwp"));
            }

            UserProfile updatedProfile = userProfileRepository.save(userProfile);
            return toResponse(updatedProfile);

        } catch (IOException e) {
            throw new BusinessException("Gagal menyimpan file: " + e.getMessage());
        }
    }

    /**
     * GET MY PROFILE
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profil belum dilengkapi"));

        return toResponse(userProfile);
    }

    /**
     * Validasi keunikan NIK, Phone, NPWP
     */
    private void validateUniqueness(String nik, String phoneNumber,
                                   String npwpNumber, Long currentProfileId) {
        // Check NIK
        userProfileRepository.findByNik(nik).ifPresent(existing -> {
            if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("NIK sudah digunakan oleh pengguna lain");
            }
        });

        // Check Phone
        userProfileRepository.findByPhoneNumber(phoneNumber).ifPresent(existing -> {
            if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor telepon sudah digunakan oleh pengguna lain");
            }
        });

        // Check NPWP
        userProfileRepository.findByNpwpNumber(npwpNumber).ifPresent(existing -> {
            if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new BusinessException("Nomor NPWP sudah digunakan oleh pengguna lain");
            }
        });
    }

    /**
     * Mapper Entity to Response DTO
     */
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

### 📝 Penjelasan Service Layer:

#### **1. @Transactional:**

```java
@Transactional
public UserProfileResponse completeProfile(...)
```

**Kenapa perlu Transaction?**

- ✅ **Atomicity**: Semua operasi sukses atau gagal semua
- ✅ **Consistency**: Database tetap konsisten
- ✅ **Rollback**: Jika error, semua perubahan dibatalkan

**Contoh Scenario:**

1. Save file KTP → ✅ Success
2. Save file Profile → ✅ Success
3. Save to database → ❌ **GAGAL**
4. **Rollback**: File yang sudah disave **tetap ada** (ini weakness, nanti kita improve)

#### **2. Validasi Keunikan:**

```java
validateUniqueness(nik, phone, npwp, currentProfileId);
```

**Parameter `currentProfileId`:**

- `null` → Complete profile (cek semua data)
- `Not null` → Update profile (kecualikan data milik user sendiri)

**Logic:**

```java
if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
    throw new BusinessException("NIK sudah digunakan");
}
```

- Jika update, boleh pakai NIK sendiri
- Jika complete, tidak boleh duplikat sama sekali

#### **3. File Upload Logic:**

```java
String ktpPath = fileStorageUtil.saveFile(request.getKtpPhoto(), "ktp");
userProfile.setKtpPhoto(ktpPath); // "ktp/uuid-xxx.jpg"
```

- Return path relatif
- Simpan ke database

#### **4. Update Logic:**

```java
if (request.getKtpPhoto() != null && !request.getKtpPhoto().isEmpty()) {
    userProfile.setKtpPhoto(fileStorageUtil.saveFile(...));
}
```

- **Conditional update**: Hanya update jika ada file baru
- Jika tidak upload, foto lama tetap dipakai

---

## 7. Controller Layer

### 📄 File: `UserProfileController.java`

**Lokasi**: `src/main/java/com/example/loanova/controller/UserProfileController.java`

```java
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * LENGKAPI PROFIL (Pertama kali)
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> completeProfile(
            @Valid @ModelAttribute UserProfileCompleteRequest request,
            Authentication authentication) {

        UserProfileResponse response = userProfileService.completeProfile(
                authentication.getName(), request);

        return ResponseUtil.created(response, "Profil berhasil dilengkapi");
    }

    /**
     * UPDATE PROFIL
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @ModelAttribute UserProfileUpdateRequest request,
            Authentication authentication) {

        UserProfileResponse response = userProfileService.updateProfile(
                authentication.getName(), request);

        return ResponseUtil.ok(response, "Profil berhasil diperbarui");
    }

    /**
     * GET MY PROFILE
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            Authentication authentication) {

        UserProfileResponse response = userProfileService.getMyProfile(
                authentication.getName());

        return ResponseUtil.ok(response, "Berhasil mengambil data profil");
    }
}
```

### 📝 Penjelasan Controller:

#### **1. @PreAuthorize:**

```java
@PreAuthorize("hasRole('CUSTOMER')")
```

- Security check di method level
- Hanya role CUSTOMER yang bisa akses
- ADMIN/MARKETING → 403 Forbidden

#### **2. Content Type:**

```java
@PostMapping(value = "/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
```

- **Wajib** untuk file upload
- Content-Type: `multipart/form-data`

#### **3. @Valid & @ModelAttribute:**

```java
@Valid @ModelAttribute UserProfileCompleteRequest request
```

- `@Valid`: Trigger validasi (NotBlank, @ValidFile, dll)
- `@ModelAttribute`: Bind multipart/form-data ke object
- Jika validasi gagal → 400 Bad Request (auto handle by GlobalExceptionHandler)

#### **4. Authentication Object:**

```java
Authentication authentication
```

- Otomatis di-inject oleh Spring Security
- Contains:
  - `authentication.getName()` → username
  - `authentication.getAuthorities()` → roles
  - `authentication.getPrincipal()` → user details

---

## 8. Configuration Layer

### 📄 File: `FileStorageUtil.java`

**Lokasi**: `src/main/java/com/example/loanova/util/FileStorageUtil.java`

```java
@Component
public class FileStorageUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Menyimpan file dengan nama UUID random
     *
     * @return Path relatif: "ktp/uuid-xxx.jpg"
     */
    public String saveFile(MultipartFile file, String subDir) throws IOException {
        // Path direktori
        Path directoryPath = Paths.get(uploadDir, subDir);

        // Buat folder jika belum ada
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Generate nama file random
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Path lengkap file
        Path filePath = directoryPath.resolve(fileName);

        // Copy file ke storage
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return path relatif: "ktp/uuid-xxx.jpg"
        return subDir + "/" + fileName;
    }
}
```

### 📝 Penjelasan File Storage:

#### **1. @Value Injection:**

```java
@Value("${file.upload-dir}")
private String uploadDir; // "uploads" dari application.properties
```

#### **2. Create Directory:**

```java
Files.createDirectories(directoryPath);
```

- Buat folder otomatis
- Struktur: `uploads/ktp/`, `uploads/profiles/`, `uploads/npwp/`

#### **3. UUID Random Filename:**

```java
String fileName = UUID.randomUUID().toString() + extension;
// Result: "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
```

**Kenapa UUID?**

- ✅ Unik (collision rate sangat kecil)
- ✅ Security (user tidak bisa tebak nama file orang lain)
- ✅ No conflict (meski upload file sama)

#### **4. Return Relative Path:**

```java
return subDir + "/" + fileName; // "ktp/uuid.jpg"
```

- Simpan di database: `"ktp/uuid.jpg"`
- Frontend construct URL: `http://localhost:9091/uploads/ktp/uuid.jpg`

---

### 📄 File: `WebConfig.java`

**Lokasi**: `src/main/java/com/example/loanova/config/WebConfig.java`

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Mapping URL /uploads/** ke direktori fisik
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

### 📝 Penjelasan Web Config:

#### **1. Resource Handler:**

```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");
```

**Mapping:**

- URL: `http://localhost:9091/uploads/ktp/uuid.jpg`
- File: `uploads/ktp/uuid.jpg` (direktori fisik)

**Cara Kerja:**

1. Request: `GET /uploads/ktp/uuid.jpg`
2. Spring MVC: Cari file di `file:uploads/ktp/uuid.jpg`
3. Return file sebagai response

---

### 📄 File: `SecurityConfig.java` (Extract)

```java
.requestMatchers(
    "/api/auth/login",
    "/api/auth/register",
    "/uploads/**") // Public access
.permitAll()
```

### 📝 Penjelasan Security:

- `/uploads/**` → **Public access**
- Foto profil bisa diakses tanpa token
- Penting untuk tampilkan foto di frontend

---

### 📄 File: `application.properties`

```properties
# Konfigurasi Multipart
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=30MB

# File Upload Directory
file.upload-dir=uploads
```

### 📝 Penjelasan Properties:

#### **1. Max File Size:**

```properties
spring.servlet.multipart.max-file-size=10MB
```

- Batas **server-level** (Spring Boot)
- Jika > 10MB → `MaxUploadSizeExceededException`
- Set lebih besar dari aplikasi (3MB) untuk custom error message

#### **2. Upload Directory:**

```properties
file.upload-dir=uploads
```

- Relative path dari project root
- Production: bisa ganti ke absolute path `/var/www/uploads`

---

## 9. Flow Diagram

### 📊 Complete Profile Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    1. CLIENT REQUEST                         │
│  POST /api/user-profiles/complete                           │
│  Content-Type: multipart/form-data                          │
│  Authorization: Bearer <token>                              │
│                                                             │
│  Body:                                                      │
│  - fullName: "John Doe"                                     │
│  - ktpPhoto: [binary data]                                  │
│  - ... (other fields)                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           2. SPRING SECURITY FILTER CHAIN                    │
│  - Extract JWT dari header                                  │
│  - Validate JWT                                             │
│  - Check role: hasRole('CUSTOMER')                          │
│  - Set Authentication di SecurityContext                    │
└────────────────────────┬────────────────────────────────────┘
                         │ ✅ Authorized
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              3. CONTROLLER LAYER                             │
│  UserProfileController.completeProfile()                    │
│                                                             │
│  - Bind request ke DTO (@ModelAttribute)                    │
│  - Trigger validasi (@Valid)                                │
│  - Extract username dari Authentication                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              4. VALIDATION LAYER                             │
│  - @NotBlank, @Size → String validation                     │
│  - @ValidFile → Custom file validation                      │
│    ├─ Check file empty                                      │
│    ├─ Check file size (< 3MB)                               │
│    └─ Check file type (image/*)                             │
└────────────────────────┬────────────────────────────────────┘
                         │ ✅ Valid
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              5. SERVICE LAYER                                │
│  UserProfileService.completeProfile()                       │
│                                                             │
│  Step 1: Find User by username                              │
│  Step 2: Check if profile already exists                    │
│  Step 3: Validate uniqueness (NIK, Phone, NPWP)            │
│  Step 4: Save files to storage                              │
│    ├─ FileStorageUtil.saveFile(ktpPhoto, "ktp")            │
│    ├─ FileStorageUtil.saveFile(profilePhoto, "profiles")   │
│    └─ FileStorageUtil.saveFile(npwpPhoto, "npwp")          │
│  Step 5: Create UserProfile entity                          │
│  Step 6: Save to database                                   │
│  Step 7: Map to Response DTO                                │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              6. RETURN RESPONSE                              │
│  HTTP 201 Created                                           │
│  {                                                          │
│    "success": true,                                         │
│    "message": "Profil berhasil dilengkapi",                 │
│    "data": {                                                │
│      "id": 1,                                               │
│      "fullName": "John Doe",                                │
│      "ktpPhoto": "ktp/uuid.jpg",                            │
│      ...                                                    │
│    }                                                        │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

### 📊 Error Handling Flow

```
┌─────────────────────────────────────────────────────────────┐
│              CLIENT REQUEST (Invalid)                        │
│  - fullName: "" (kosong)                                    │
│  - ktpPhoto: document.pdf (bukan gambar)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              VALIDATION LAYER                                │
│  ❌ Validation Failed                                        │
│  - fullName: "Nama lengkap wajib diisi"                     │
│  - ktpPhoto: "File harus berupa gambar (JPG/PNG)"           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         MethodArgumentNotValidException                      │
│         (Auto thrown by Spring)                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         GlobalExceptionHandler                               │
│  @ExceptionHandler(MethodArgumentNotValidException.class)   │
│                                                             │
│  - Extract field errors dari BindingResult                  │
│  - Build error map                                          │
│  - Return 400 Bad Request                                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              ERROR RESPONSE                                  │
│  HTTP 400 Bad Request                                       │
│  {                                                          │
│    "success": false,                                        │
│    "message": "Validasi gagal",                             │
│    "data": {                                                │
│      "errors": {                                            │
│        "fullName": "Nama lengkap wajib diisi",              │
│        "ktpPhoto": "File harus berupa gambar (JPG/PNG)"     │
│      }                                                      │
│    }                                                        │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. Best Practices

### ✅ 1. Separation of Concerns

**Setiap layer punya tanggung jawab sendiri:**

| Layer          | Tanggung Jawab                         |
| -------------- | -------------------------------------- |
| **Controller** | Handle HTTP request/response           |
| **Service**    | Business logic, transaction management |
| **Repository** | Database operations                    |
| **Validation** | Input validation rules                 |
| **Util**       | Helper functions (file storage)        |

**Jangan:**

- ❌ Business logic di Controller
- ❌ Database query di Controller
- ❌ File handling di Service (pakai Util)

---

### ✅ 2. DTO Pattern

**Gunakan DTO untuk:**

- Request (input dari client)
- Response (output ke client)

**Jangan:**

- ❌ Expose Entity langsung ke client
- ❌ Pakai Entity sebagai request body

**Keuntungan:**

- ✅ Security (hide internal structure)
- ✅ Flexibility (beda input/output)
- ✅ Validation (terpisah dari Entity)

---

### ✅ 3. Custom Validator

**Buat custom validator untuk:**

- Complex validation logic
- Reusable validation
- Cross-field validation

**Contoh:**

```java
@ValidFile(required = true, maxSize = 3MB)
private MultipartFile ktpPhoto;
```

**Keuntungan:**

- ✅ Declarative (easy to read)
- ✅ Reusable (pakai di banyak field)
- ✅ Testable (unit test validator)

---

### ✅ 4. Transaction Management

**Gunakan @Transactional untuk:**

- Multiple database operations
- Rollback on error
- Consistency guarantee

```java
@Transactional
public UserProfileResponse completeProfile(...) {
    // All operations in one transaction
    // Rollback if any operation fails
}
```

**Read-only optimization:**

```java
@Transactional(readOnly = true)
public UserProfileResponse getMyProfile(...) {
    // Read-only transaction (performance boost)
}
```

---

### ✅ 5. File Upload Security

**UUID Random Filename:**

```java
String fileName = UUID.randomUUID().toString() + extension;
```

**Kenapa?**

- ✅ Prevent path traversal attack
- ✅ No file name collision
- ✅ Hide original filename

**Validate File Type:**

```java
if (!contentType.startsWith("image/")) {
    throw new ValidationException();
}
```

**Limit File Size:**

```java
if (file.getSize() > 3 * 1024 * 1024) {
    throw new ValidationException();
}
```

---

### ✅ 6. Error Handling

**Centralized exception handling:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(...) {
        // Handle all validation errors
    }
}
```

**Custom exceptions:**

```java
throw new BusinessException("NIK sudah digunakan");
```

**Consistent error format:**

```json
{
  "success": false,
  "message": "Error message",
  "data": { "errors": {...} }
}
```

---

### ✅ 7. Security

**Role-based access:**

```java
@PreAuthorize("hasRole('CUSTOMER')")
```

**Public access untuk file:**

```java
.requestMatchers("/uploads/**").permitAll()
```

**Token validation:**

- JWT di setiap request
- Automatic validation oleh filter

---

### ✅ 8. Code Organization

**Package structure:**

```
src/main/java/com/example/loanova/
├── config/          # Configuration classes
├── controller/      # REST endpoints
├── dto/
│   ├── request/     # Input DTOs
│   └── response/    # Output DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions
├── repository/      # Data access
├── service/         # Business logic
├── util/            # Helper classes
└── validation/      # Custom validators
```

---

## 🎯 Kesimpulan

### ✅ Fitur Lengkap:

- Complete profile (pertama kali)
- Update profile (dengan file opsional)
- Get my profile
- File upload dengan validasi
- Security dengan role-based access

### ✅ Best Practices Diterapkan:

- Separation of concerns
- DTO pattern
- Custom validator
- Transaction management
- Centralized error handling
- Security best practices

### ✅ Validasi Komprehensif:

- Input validation (@NotBlank, @Size)
- File validation (size, type, empty)
- Business validation (uniqueness)
- **Semua error muncul bersamaan**

### 🚀 Production Ready!

**File yang Digunakan:**

1. Entity: UserProfile.java
2. DTO: UserProfileCompleteRequest, UserProfileUpdateRequest, UserProfileResponse
3. Repository: UserProfileRepository.java
4. Validation: ValidFile.java, ValidFileValidator.java
5. Service: UserProfileService.java
6. Controller: UserProfileController.java
7. Config: WebConfig.java, SecurityConfig.java
8. Util: FileStorageUtil.java

**Total: 11 files dengan arsitektur yang clean dan maintainable!** 🎉
