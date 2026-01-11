# cURL Commands untuk User Plafond API

## Setup

Base URL: `http://localhost:9090`
Pastikan Anda sudah login sebagai SUPERADMIN atau BACKOFFICE untuk mendapatkan token.

---

## 1. Assign Plafond ke User (SUPERADMIN Only)

### Request

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN_HERE' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

### Contoh dengan Data Berbeda

**Assign Bronze (12 juta) ke User ID 10:**

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN_HERE' \
--data '{
    "userId": 10,
    "plafondId": 3,
    "maxAmount": 12000000.00
}'
```

**Assign Silver (50 juta) ke User ID 5:**

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN_HERE' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

**Assign Gold (100 juta) dengan custom amount (75 juta):**

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN_HERE' \
--data '{
    "userId": 8,
    "plafondId": 1,
    "maxAmount": 75000000.00
}'
```

### Response Success (201 Created)

```json
{
  "success": true,
  "message": "Plafond berhasil di-assign ke user",
  "data": {
    "id": 15,
    "userId": 5,
    "username": "customer01",
    "plafondId": 2,
    "plafondName": "Silver",
    "maxAmount": 50000000.0,
    "remainingAmount": 50000000.0,
    "isActive": true,
    "assignedAt": "2026-01-11T10:30:00"
  },
  "timestamp": "2026-01-11T10:30:00"
}
```

### Error Responses

**User tidak ditemukan (404):**

```json
{
  "success": false,
  "message": "User dengan ID 999 tidak ditemukan",
  "timestamp": "2026-01-11T10:30:00"
}
```

**Plafond tidak ditemukan (404):**

```json
{
  "success": false,
  "message": "Plafond dengan ID 999 tidak ditemukan",
  "timestamp": "2026-01-11T10:30:00"
}
```

**Max amount melebihi limit (400):**

```json
{
  "success": false,
  "message": "Max amount yang diberikan (100000000) melebihi max amount plafond Silver (50000000)",
  "timestamp": "2026-01-11T10:30:00"
}
```

**Validasi gagal (400):**

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "userId": "User ID wajib diisi",
    "maxAmount": "Max amount harus lebih besar dari 0"
  },
  "timestamp": "2026-01-11T10:30:00"
}
```

---

## 2. Get Active User Plafond (SUPERADMIN, BACKOFFICE)

### Request

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/5/active' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Contoh dengan User ID Berbeda

**Get plafond user ID 10:**

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/10/active' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

**Get plafond user ID 8:**

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/8/active' \
--header 'Authorization: Bearer YOUR_TOKEN_HERE'
```

### Response Success (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil data plafond user",
  "data": {
    "id": 15,
    "userId": 5,
    "username": "customer01",
    "plafondId": 2,
    "plafondName": "Silver",
    "maxAmount": 50000000.0,
    "remainingAmount": 35000000.0,
    "isActive": true,
    "assignedAt": "2026-01-11T10:30:00"
  },
  "timestamp": "2026-01-11T10:30:00"
}
```

### Error Responses

**User tidak ditemukan (404):**

```json
{
  "success": false,
  "message": "User dengan ID 999 tidak ditemukan",
  "timestamp": "2026-01-11T10:30:00"
}
```

**User tidak punya plafond aktif (404):**

```json
{
  "success": false,
  "message": "User tidak memiliki plafond aktif. Silakan assign plafond terlebih dahulu",
  "timestamp": "2026-01-11T10:30:00"
}
```

---

## 3. Flow Testing Lengkap

### Step 1: Login sebagai SUPERADMIN

```bash
curl --location 'http://localhost:9090/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "superadmin",
    "password": "superadmin123"
}'
```

**Simpan accessToken dari response untuk digunakan di request selanjutnya.**

### Step 2: Assign Plafond Bronze ke Customer Baru

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
--data '{
    "userId": 5,
    "plafondId": 3,
    "maxAmount": 12000000.00
}'
```

### Step 3: Cek Plafond yang Baru Di-assign

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/5/active' \
--header 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### Step 4: Upgrade Plafond ke Silver (plafond Bronze akan otomatis non-aktif)

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

### Step 5: Verifikasi Plafond Sudah Berubah ke Silver

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/5/active' \
--header 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

---

## 4. Test Error Scenarios

### Test 1: Assign dengan Max Amount Melebihi Limit Plafond

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 100000000.00
}'
```

**Expected:** 400 Bad Request - Max amount melebihi limit Silver (50 juta)

### Test 2: Assign ke User yang Tidak Exist

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN' \
--data '{
    "userId": 99999,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

**Expected:** 404 Not Found - User tidak ditemukan

### Test 3: Assign dengan Plafond yang Tidak Exist

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN' \
--data '{
    "userId": 5,
    "plafondId": 99999,
    "maxAmount": 50000000.00
}'
```

**Expected:** 404 Not Found - Plafond tidak ditemukan

### Test 4: Get Plafond User yang Belum Punya Plafond Aktif

```bash
curl --location 'http://localhost:9090/api/user-plafonds/users/100/active' \
--header 'Authorization: Bearer YOUR_SUPERADMIN_TOKEN'
```

**Expected:** 404 Not Found - User tidak punya plafond aktif

### Test 5: Akses tanpa Token (Unauthorized)

```bash
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

**Expected:** 401 Unauthorized

### Test 6: Akses dengan Role CUSTOMER (Forbidden)

```bash
# Login sebagai customer dulu
curl --location 'http://localhost:9090/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "customer01",
    "password": "customer123"
}'

# Coba assign plafond dengan token customer
curl --location 'http://localhost:9090/api/user-plafonds/assign' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer CUSTOMER_TOKEN' \
--data '{
    "userId": 5,
    "plafondId": 2,
    "maxAmount": 50000000.00
}'
```

**Expected:** 403 Forbidden - Customer tidak punya akses

---

## 5. Import ke Postman

### Cara Import Collection:

1. Buka Postman
2. Klik **Import** button
3. Pilih tab **Raw text**
4. Copy paste salah satu curl command di atas
5. Klik **Continue** lalu **Import**
6. Atau buat collection manual dengan struktur berikut:

**Collection: User Plafond API**

```
├── Auth
│   └── Login SUPERADMIN
├── User Plafond
│   ├── Assign Plafond to User
│   ├── Get Active User Plafond
│   └── Test Cases
│       ├── Assign Bronze
│       ├── Assign Silver
│       ├── Assign Gold
│       ├── Test Max Amount Exceed
│       └── Test User Not Found
```

### Environment Variables (Opsional)

Buat environment di Postman dengan variables:

```
baseUrl = http://localhost:9090
superadminToken = (paste token setelah login)
backofficeToken = (paste token setelah login)
userId = 5
plafondId = 2
```

Lalu gunakan di request:

```
{{baseUrl}}/api/user-plafonds/assign
Authorization: Bearer {{superadminToken}}
```

---

## 6. Sample IDs (Sesuaikan dengan Database Anda)

### Plafond IDs:

- **1** = Gold (100.000.000)
- **2** = Silver (50.000.000)
- **3** = Bronze (12.000.000)
- **4** = Platinum (200.000.000)

### User IDs dengan Role CUSTOMER:

- **5** = customer01
- **8** = customer02
- **10** = customer03

**Note:** ID ini contoh, sesuaikan dengan data di database Anda.

---

## Tips Testing:

1. **Simpan Token:** Setelah login, simpan accessToken di notepad atau Postman environment variable
2. **Replace Token:** Ganti `YOUR_SUPERADMIN_TOKEN_HERE` dengan token yang sebenarnya
3. **Check Database:** Setelah assign, cek tabel user_plafond untuk melihat perubahannya
4. **Test Error Cases:** Jangan lupa test semua error scenario untuk memastikan validasi berjalan
5. **Sequential Testing:** Test dengan urutan: assign → get → upgrade → get lagi untuk melihat flow lengkap

---

**Created:** 11 Januari 2026  
**Project:** LoanOva - User Plafond Management
