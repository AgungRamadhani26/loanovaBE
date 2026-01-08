# TEST CASES - USER PROFILE ERROR VALIDATION

## 📋 ENDPOINT: POST /api/user-profiles/complete

### ✅ TEST CASE 1: Semua Field Kosong (All Errors Together)

**Tujuan**: Mengetes bahwa semua error muncul bersamaan dalam 1 response

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data
Authorization: Bearer <token_customer>

Body (form-data):
fullName:
phoneNumber:
userAddress:
nik:
birthDate:
npwpNumber:
ktpPhoto: (tidak diisi/kosong)
profilePhoto: (tidak diisi/kosong)
npwpPhoto: (tidak diisi/kosong)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "fullName": "Nama lengkap wajib diisi",
      "phoneNumber": "Nomor telepon wajib diisi",
      "userAddress": "Alamat wajib diisi",
      "nik": "NIK wajib diisi",
      "birthDate": "Tanggal lahir wajib diisi",
      "npwpNumber": "Nomor NPWP wajib diisi",
      "ktpPhoto": "File wajib diunggah",
      "profilePhoto": "File wajib diunggah",
      "npwpPhoto": "File wajib diunggah"
    }
  }
}
```

---

### ❌ TEST CASE 2: NIK Tidak Valid (Kurang dari 16 digit)

**Tujuan**: Mengetes validasi panjang NIK

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 12345 (hanya 5 digit)
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: (file gambar valid)
profilePhoto: (file gambar valid)
npwpPhoto: (file gambar valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "nik": "NIK harus 16 karakter"
    }
  }
}
```

---

### ❌ TEST CASE 3: File KTP Bukan Gambar (PDF/DOCX)

**Tujuan**: Mengetes validasi tipe file

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 1234567890123456
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: document.pdf (file PDF, bukan gambar)
profilePhoto: (file gambar valid)
npwpPhoto: (file gambar valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "ktpPhoto": "File harus berupa gambar (JPG/PNG)"
    }
  }
}
```

---

### ❌ TEST CASE 4: File Terlalu Besar (> 3MB)

**Tujuan**: Mengetes validasi ukuran file

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 1234567890123456
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: large_image.jpg (ukuran 5MB)
profilePhoto: (file gambar valid < 3MB)
npwpPhoto: (file gambar valid < 3MB)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "ktpPhoto": "Ukuran file terlalu besar. Maksimal 3MB"
    }
  }
}
```

---

### ❌ TEST CASE 5: Multiple File Errors

**Tujuan**: Mengetes error multiple file sekaligus

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 1234567890123456
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: (kosong)
profilePhoto: document.pdf (bukan gambar)
npwpPhoto: large.jpg (> 3MB)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "ktpPhoto": "File wajib diunggah",
      "profilePhoto": "File harus berupa gambar (JPG/PNG)",
      "npwpPhoto": "Ukuran file terlalu besar. Maksimal 3MB"
    }
  }
}
```

---

### ❌ TEST CASE 6: NIK Sudah Digunakan (Duplicate)

**Tujuan**: Mengetes validasi keunikan NIK

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 1234567890123456 (NIK yang sudah terdaftar)
birthDate: 1990-01-01
npwpNumber: 9999999999999999
ktpPhoto: (file gambar valid)
profilePhoto: (file gambar valid)
npwpPhoto: (file gambar valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "NIK sudah digunakan oleh pengguna lain",
  "data": null
}
```

---

### ❌ TEST CASE 7: Nomor Telepon Sudah Digunakan

**Tujuan**: Mengetes validasi keunikan nomor telepon

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890 (nomor yang sudah terdaftar)
userAddress: Jl. Sudirman No. 123
nik: 9999999999999999
birthDate: 1990-01-01
npwpNumber: 9999999999999999
ktpPhoto: (file gambar valid)
profilePhoto: (file gambar valid)
npwpPhoto: (file gambar valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Nomor telepon sudah digunakan oleh pengguna lain",
  "data": null
}
```

---

### ❌ TEST CASE 8: NPWP Sudah Digunakan

**Tujuan**: Mengetes validasi keunikan NPWP

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567899
userAddress: Jl. Sudirman No. 123
nik: 9999999999999999
birthDate: 1990-01-01
npwpNumber: 1234567890123456 (NPWP yang sudah terdaftar)
ktpPhoto: (file gambar valid)
profilePhoto: (file gambar valid)
npwpPhoto: (file gambar valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Nomor NPWP sudah digunakan oleh pengguna lain",
  "data": null
}
```

---

### ❌ TEST CASE 9: Profil Sudah Dilengkapi Sebelumnya

**Tujuan**: Mengetes user tidak bisa complete profile dua kali

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
(User sudah pernah complete profile sebelumnya)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Profil sudah dilengkapi. Gunakan fitur update untuk mengubah data.",
  "data": null
}
```

---

### ❌ TEST CASE 10: Nama Terlalu Panjang (> 100 karakter)

**Tujuan**: Mengetes validasi max length

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data

Body:
fullName: "John Doe Smith Anderson Williams Taylor Thomas Jackson White Harris Martin Thompson Garcia Martinez Robinson Clark Rodriguez Lewis Lee Walker Hall Allen Young Hernandez King Wright Lopez Hill Scott Green Adams Baker Gonzalez Nelson Carter Mitchell Perez Roberts Turner Phillips Campbell Parker Evans Edwards Collins Stewart Sanchez Morris Rogers Reed Cook Morgan Bell Murphy Bailey Rivera Cooper Richardson Cox Howard Ward Torres Peterson Gray Ramirez James Watson Brooks Kelly Sanders Price Bennett Wood Barnes Ross Henderson Coleman Jenkins Perry Powell Long Patterson Hughes Flores Washington Butler Simmons Foster Gonzales Bryant Alexander Russell Griffin Diaz Hayes Myers Ford Hamilton Graham Sullivan Wallace Woods Cole West Jordan Owens Reynolds Fisher Ellis Harrison Gibson Mcdonald Cruz Marshall Ortiz Gomez Murray Freeman Wells Webb Simpson Stevens Tucker Porter Hunter Hicks Crawford Henry Boyd Mason Morales Kennedy Warren Dixon Ramos Reyes Burns Gordon Shaw Holmes Rice Robertson Hunt Black Daniels Palmer Mills Nichols Grant Knight Ferguson Rose Stone Hawkins Dunn Perkins Hudson Spencer Gardner Stephens Payne Pierce Berry Matthews Arnold Wagner Willis Ray Watkins Olson Carroll Duncan Snyder Hart Cunningham Bradley Lane Andrews Ruiz Harper Fox Riley Armstrong Carpenter Weaver Greene Lawrence Elliott Chavez Sims Austin Peters Kelley Franklin Lawson Fields Gutierrez Ryan Schmidt Carr Vasquez Castillo Wheeler Chapman Oliver Montgomery Richards Williamson Johnston Banks Meyer Bishop Mccoy Howell Alvarez Morrison Hansen Fernandez Garza Harvey Little Burton Stanley Nguyen George Jacobs Reid Kim Fuller Lynch Dean Gilbert Garrett Romero Welch Larson Frazier Burke Hanson Day Mendoza Moreno Bowman Medina Fowler Brewer Hoffman Carlson Silva Pearson Holland Douglas Fleming Jensen Vargas Byrd Davidson Hopkins May Terry Herrera Wade Soto Walters Curtis Neal Caldwell Lowe Jennings Barnett Graves Jimenez Horton Shelton Barrett Obrien Castro Sutton Gregory Mckinney Lucas Miles Craig Rodriquez Chambers Holt Lambert Fletcher Watts Barclay"
phoneNumber: 081234567890
... (field lainnya valid)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "fullName": "Nama lengkap maksimal 100 karakter"
    }
  }
}
```

---

## 📋 ENDPOINT: PUT /api/user-profiles/update

### ✅ TEST CASE 11: Update Tanpa File (File Opsional)

**Tujuan**: Mengetes update profil tanpa upload file baru

**Request**:

```
PUT http://localhost:9091/api/user-profiles/update
Content-Type: multipart/form-data
Authorization: Bearer <token_customer>

Body:
fullName: John Doe Updated
phoneNumber: 081234567891
userAddress: Jl. Sudirman No. 456
nik: 1234567890123456
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: (tidak diisi - akan tetap pakai yang lama)
profilePhoto: (tidak diisi)
npwpPhoto: (tidak diisi)
```

**Expected Response** (200 OK):

```json
{
  "success": true,
  "message": "Profil berhasil diperbarui",
  "data": {
    "id": 1,
    "fullName": "John Doe Updated",
    "ktpPhoto": "ktp/old-uuid.jpg" (foto lama tetap)
  }
}
```

---

### ❌ TEST CASE 12: Update File Bukan Gambar

**Tujuan**: Mengetes validasi file saat update

**Request**:

```
PUT http://localhost:9091/api/user-profiles/update
Content-Type: multipart/form-data

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123
nik: 1234567890123456
birthDate: 1990-01-01
npwpNumber: 1234567890123456
ktpPhoto: document.pdf (bukan gambar)
profilePhoto: (tidak diisi)
npwpPhoto: (tidak diisi)
```

**Expected Response** (400 Bad Request):

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "ktpPhoto": "File harus berupa gambar (JPG/PNG)"
    }
  }
}
```

---

### ❌ TEST CASE 13: Update Profil Belum Dilengkapi

**Tujuan**: Mengetes user harus complete dulu sebelum update

**Request**:

```
PUT http://localhost:9091/api/user-profiles/update
(User belum pernah complete profile)
```

**Expected Response** (404 Not Found):

```json
{
  "success": false,
  "message": "Profil belum dilengkapi. Silakan lengkapi profil terlebih dahulu.",
  "data": null
}
```

---

## 📋 ENDPOINT: GET /api/user-profiles/me

### ❌ TEST CASE 14: Get Profile Sebelum Complete

**Tujuan**: Mengetes user belum punya profil

**Request**:

```
GET http://localhost:9091/api/user-profiles/me
Authorization: Bearer <token_customer>
```

**Expected Response** (404 Not Found):

```json
{
  "success": false,
  "message": "Profil belum dilengkapi",
  "data": null
}
```

---

### ❌ TEST CASE 15: Tanpa Authorization Token

**Tujuan**: Mengetes endpoint protected

**Request**:

```
GET http://localhost:9091/api/user-profiles/me
(Tanpa header Authorization)
```

**Expected Response** (401 Unauthorized):

```json
{
  "success": false,
  "message": "Autentikasi gagal. Silakan login terlebih dahulu",
  "data": null
}
```

---

### ❌ TEST CASE 16: Role Bukan CUSTOMER

**Tujuan**: Mengetes endpoint khusus role CUSTOMER

**Request**:

```
POST http://localhost:9091/api/user-profiles/complete
Authorization: Bearer <token_admin> (role ADMIN atau MARKETING)
```

**Expected Response** (403 Forbidden):

```json
{
  "success": false,
  "message": "Anda tidak memiliki akses untuk mengakses resource ini",
  "data": null
}
```

---

## 🎯 CARA TESTING DI POSTMAN

### Setup Environment:

1. Buat environment variable `base_url` = `http://localhost:9091`
2. Buat variable `token_customer` untuk menyimpan JWT token

### Langkah Testing:

#### 1. Login sebagai CUSTOMER:

```
POST {{base_url}}/api/auth/login
Body (JSON):
{
  "username": "customer1",
  "password": "password123"
}
```

Copy `accessToken` dari response ke variable `token_customer`

#### 2. Test Case Semua Field Kosong:

```
POST {{base_url}}/api/user-profiles/complete
Authorization: Bearer {{token_customer}}
Body (form-data):
- fullName: (kosongkan)
- phoneNumber: (kosongkan)
- userAddress: (kosongkan)
- nik: (kosongkan)
- birthDate: (kosongkan)
- npwpNumber: (kosongkan)
- ktpPhoto: (jangan pilih file)
- profilePhoto: (jangan pilih file)
- npwpPhoto: (jangan pilih file)
```

#### 3. Test File Bukan Gambar:

- Siapkan file PDF atau DOCX
- Upload sebagai ktpPhoto
- Akan muncul error: "File harus berupa gambar (JPG/PNG)"

#### 4. Test File Terlalu Besar:

- Siapkan gambar > 3MB
- Upload sebagai ktpPhoto
- Akan muncul error: "Ukuran file terlalu besar. Maksimal 3MB"

---

## 📝 EXPECTED BEHAVIOR SUMMARY

| Test Case            | Expected Status | Error Message Location                    |
| -------------------- | --------------- | ----------------------------------------- |
| Semua field kosong   | 400             | errors.fullName, errors.ktpPhoto, dll     |
| NIK < 16 karakter    | 400             | errors.nik                                |
| File bukan gambar    | 400             | errors.ktpPhoto                           |
| File > 3MB           | 400             | errors.ktpPhoto                           |
| Multiple file errors | 400             | errors.ktpPhoto, errors.profilePhoto, dll |
| NIK duplikat         | 400             | message (root level)                      |
| Phone duplikat       | 400             | message (root level)                      |
| NPWP duplikat        | 400             | message (root level)                      |
| Profil sudah ada     | 400             | message (root level)                      |
| Update tanpa file    | 200             | Success                                   |
| Get tanpa auth       | 401             | message (root level)                      |
| Role salah           | 403             | message (root level)                      |

---

## ✅ VALIDASI YANG HARUS LOLOS:

**Valid Request Example:**

```
POST http://localhost:9091/api/user-profiles/complete
Content-Type: multipart/form-data
Authorization: Bearer <token_customer>

Body:
fullName: John Doe
phoneNumber: 081234567890
userAddress: Jl. Sudirman No. 123, Jakarta
nik: 3201234567890123 (16 digit)
birthDate: 1990-01-01
npwpNumber: 1234567890123456 (maks 16 digit)
ktpPhoto: ktp.jpg (gambar < 3MB)
profilePhoto: profile.jpg (gambar < 3MB)
npwpPhoto: npwp.jpg (gambar < 3MB)
```

**Expected Response** (201 Created):

```json
{
  "success": true,
  "message": "Profil berhasil dilengkapi",
  "data": {
    "id": 1,
    "userId": 5,
    "username": "customer1",
    "fullName": "John Doe",
    "phoneNumber": "081234567890",
    "userAddress": "Jl. Sudirman No. 123, Jakarta",
    "nik": "3201234567890123",
    "birthDate": "1990-01-01",
    "npwpNumber": "1234567890123456",
    "ktpPhoto": "ktp/uuid-xxx.jpg",
    "profilePhoto": "profiles/uuid-yyy.jpg",
    "npwpPhoto": "npwp/uuid-zzz.jpg",
    "createdAt": "2026-01-08T10:30:00",
    "updatedAt": null
  }
}
```

**Akses Foto:**

- `http://localhost:9091/uploads/ktp/uuid-xxx.jpg`
- `http://localhost:9091/uploads/profiles/uuid-yyy.jpg`
- `http://localhost:9091/uploads/npwp/uuid-zzz.jpg`

---

**SELAMAT TESTING! 🚀**
