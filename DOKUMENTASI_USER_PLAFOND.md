# Dokumentasi Fitur User Plafond Assignment

## Overview

Fitur ini memungkinkan SUPERADMIN untuk assign plafond (limit pinjaman) kepada user dengan role CUSTOMER. Sistem mengelola plafond aktif untuk setiap user, dimana setiap user hanya dapat memiliki 1 plafond aktif pada satu waktu.

---

## Alur Bisnis

### 1. Auto-Assignment Plafond Bronze saat Registrasi

Ketika user baru mendaftar dengan role CUSTOMER:

- Otomatis dibuatkan user_plafond dengan plafond **Bronze (ID: 3)**
- `maxAmount` dan `remainingAmount` diambil dari maxAmount plafond Bronze (misalnya: Rp 12.000.000)
- Status `isActive` = true

### 2. Manual Assignment oleh SUPERADMIN

SUPERADMIN dapat assign plafond baru ke user kapan saja:

- Pilih user (customer) yang akan diberi plafond
- Pilih paket plafond (Bronze/Silver/Gold/Platinum)
- Tentukan `maxAmount` yang akan diberikan (tidak boleh melebihi maxAmount dari paket plafond)
- Plafond lama yang masih aktif akan otomatis dinonaktifkan
- Plafond baru akan dibuat dengan status aktif

### 3. Penggunaan Plafond

- Customer hanya bisa mengajukan pinjaman ≤ `remainingAmount`
- Setiap kali melakukan peminjaman, `remainingAmount` akan berkurang
- Remaining amount **TIDAK akan bertambah** ketika customer melunasi pinjaman
- Jika remaining amount habis, customer harus menunggu SUPERADMIN assign plafond baru

---

## API Endpoints

### 1. Assign Plafond ke User

**Endpoint:** `POST /api/user-plafonds/assign`  
**Authorization:** SUPERADMIN only  
**Content-Type:** application/json

**Request Body:**

```json
{
  "userId": 5,
  "plafondId": 2,
  "maxAmount": 50000000.0
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Plafond berhasil di-assign ke user",
  "data": {
    "id": 10,
    "userId": 5,
    "username": "customer01",
    "plafondId": 2,
    "plafondName": "Silver",
    "maxAmount": 50000000.0,
    "remainingAmount": 50000000.0,
    "isActive": true,
    "assignedAt": "2026-01-10T10:30:00"
  },
  "timestamp": "2026-01-10T10:30:00"
}
```

**Validasi:**

- `userId` wajib diisi dan harus positif
- `plafondId` wajib diisi dan harus positif
- `maxAmount` wajib diisi dan harus > 0
- `maxAmount` tidak boleh melebihi maxAmount dari paket plafond yang dipilih
- User dan plafond harus exist di database

**Error Responses:**

_User tidak ditemukan (404):_

```json
{
  "success": false,
  "message": "User dengan ID 5 tidak ditemukan",
  "timestamp": "2026-01-10T10:30:00"
}
```

_Max amount melebihi limit plafond (400):_

```json
{
  "success": false,
  "message": "Max amount yang diberikan (100000000) melebihi max amount plafond Silver (50000000)",
  "timestamp": "2026-01-10T10:30:00"
}
```

---

### 2. Get Active User Plafond

**Endpoint:** `GET /api/user-plafonds/users/{userId}/active`  
**Authorization:** SUPERADMIN, BACKOFFICE

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil data plafond user",
  "data": {
    "id": 10,
    "userId": 5,
    "username": "customer01",
    "plafondId": 2,
    "plafondName": "Silver",
    "maxAmount": 50000000.0,
    "remainingAmount": 35000000.0,
    "isActive": true,
    "assignedAt": "2026-01-10T10:30:00"
  },
  "timestamp": "2026-01-10T10:30:00"
}
```

**Error Response (404):**

```json
{
  "success": false,
  "message": "User tidak memiliki plafond aktif. Silakan assign plafond terlebih dahulu",
  "timestamp": "2026-01-10T10:30:00"
}
```

---

## Database Schema

### Table: user_plafond

| Column           | Type                   | Description                       |
| ---------------- | ---------------------- | --------------------------------- |
| id               | BIGINT (PK, Auto)      | Primary key                       |
| user_id          | BIGINT (FK, NOT NULL)  | Foreign key ke users table        |
| plafond_id       | BIGINT (FK, NOT NULL)  | Foreign key ke plafonds table     |
| max_amount       | DECIMAL(18,2) NOT NULL | Jumlah maksimal yang disetujui    |
| remaining_amount | DECIMAL(18,2) NOT NULL | Sisa plafond yang tersedia        |
| is_active        | BOOLEAN NOT NULL       | Status aktif plafond              |
| assigned_at      | DATETIME NOT NULL      | Timestamp kapan plafond di-assign |

**Relasi:**

- `users` (1) -> `user_plafond` (N): One user can have many plafond assignments
- `plafonds` (1) -> `user_plafond` (N): One plafond package can be assigned to many users

---

## Business Rules

### 1. Plafond Aktif

- Setiap user hanya boleh memiliki **1 plafond aktif** pada satu waktu
- Ketika plafond baru di-assign, plafond lama otomatis dinonaktifkan (`isActive = false`)
- Plafond yang tidak aktif tetap tersimpan di database untuk keperluan audit

### 2. Max Amount

- Max amount yang diberikan ke user bisa sama atau **lebih kecil** dari maxAmount paket plafond
- Contoh: Paket Silver maxAmount 50jt, tapi SUPERADMIN bisa assign 30jt ke user tertentu
- Validasi: maxAmount user tidak boleh melebihi maxAmount paket plafond

### 3. Remaining Amount

- Saat pertama kali di-assign: `remainingAmount = maxAmount`
- Berkurang saat customer mengajukan pinjaman
- **TIDAK bertambah** saat customer melunasi pinjaman
- Jika habis (= 0), customer tidak bisa mengajukan pinjaman baru

### 4. Assignment History

- Semua assignment plafond tersimpan di database (baik aktif maupun tidak aktif)
- Bisa digunakan untuk tracking histori perubahan plafond user
- Field `assignedAt` mencatat kapan plafond di-assign

---

## Use Cases

### Use Case 1: Customer Baru Registrasi

```
1. Customer register melalui endpoint /api/auth/register
2. Sistem otomatis create user dengan role CUSTOMER
3. Sistem otomatis create user_plafond dengan:
   - plafond = Bronze (ID: 3)
   - maxAmount = maxAmount Bronze (12.000.000)
   - remainingAmount = 12.000.000
   - isActive = true
```

### Use Case 2: SUPERADMIN Upgrade Plafond Customer

```
Customer sudah punya Bronze (12jt), SUPERADMIN ingin upgrade ke Silver (50jt):

1. SUPERADMIN hit endpoint POST /api/user-plafonds/assign
   Request:
   {
     "userId": 5,
     "plafondId": 2,  // Silver
     "maxAmount": 50000000
   }

2. Sistem:
   - Nonaktifkan plafond Bronze lama (set isActive = false)
   - Create plafond Silver baru:
     * maxAmount = 50.000.000
     * remainingAmount = 50.000.000
     * isActive = true

3. Customer sekarang punya limit 50jt (fresh, belum terpakai)
```

### Use Case 3: Customer Ajukan Pinjaman (Akan Dibuat)

```
Customer dengan plafond aktif Silver (remaining 50jt) ajukan pinjaman 15jt:

1. Sistem cek plafond aktif customer
2. Validasi: 15jt <= 50jt (remaining) ✓
3. Proses loan application
4. Kurangi remaining amount: 50jt - 15jt = 35jt
5. Update user_plafond set remaining_amount = 35.000.000

Customer sekarang remaining 35jt, bisa ajukan pinjaman lagi max 35jt.
```

### Use Case 4: Remaining Amount Habis

```
Customer dengan remaining 5jt, ajukan pinjaman 5jt:

1. Loan approved, remaining = 0
2. Customer coba ajukan pinjaman lagi
3. Sistem reject: remaining amount tidak mencukupi
4. Customer harus tunggu SUPERADMIN assign plafond baru

SUPERADMIN bisa:
- Assign plafond baru (upgrade/downgrade)
- Atau assign plafond yang sama dengan maxAmount baru
```

---

## Testing dengan Postman

### 1. Setup

```
1. Login sebagai SUPERADMIN untuk mendapatkan token
2. Tambahkan Authorization header: Bearer <token>
```

### 2. Test Assign Plafond

```
POST http://localhost:9090/api/user-plafonds/assign
Content-Type: application/json
Authorization: Bearer <token-superadmin>

Body:
{
  "userId": 5,
  "plafondId": 2,
  "maxAmount": 50000000
}

Expected: 201 Created
```

### 3. Test Get Active Plafond

```
GET http://localhost:9090/api/user-plafonds/users/5/active
Authorization: Bearer <token-superadmin-or-backoffice>

Expected: 200 OK dengan data plafond aktif
```

### 4. Test Error Handling

```
Test 1: Max amount melebihi limit
{
  "userId": 5,
  "plafondId": 2,  // Silver max 50jt
  "maxAmount": 100000000  // 100jt (exceed)
}
Expected: 400 Bad Request

Test 2: User tidak exist
{
  "userId": 99999,
  "plafondId": 2,
  "maxAmount": 50000000
}
Expected: 404 Not Found

Test 3: Plafond tidak exist
{
  "userId": 5,
  "plafondId": 99999,
  "maxAmount": 50000000
}
Expected: 404 Not Found
```

---

## Code Structure

### DTO Request

**File:** `AssignUserPlafondRequest.java`

```java
- userId: Long (wajib, positif)
- plafondId: Long (wajib, positif)
- maxAmount: BigDecimal (wajib, > 0)
```

### DTO Response

**File:** `UserPlafondResponse.java`

```java
- id: Long
- userId: Long
- username: String
- plafondId: Long
- plafondName: String
- maxAmount: BigDecimal
- remainingAmount: BigDecimal
- isActive: Boolean
- assignedAt: LocalDateTime
```

### Service

**File:** `UserPlafondService.java`

**Methods:**

1. `assignPlafondToUser(request)` - Assign plafond ke user
2. `getActiveUserPlafond(userId)` - Get plafond aktif user
3. `toResponse(userPlafond)` - Mapper entity ke DTO

### Controller

**File:** `UserPlafondController.java`

**Endpoints:**

1. `POST /assign` - Assign plafond (SUPERADMIN only)
2. `GET /users/{userId}/active` - Get active plafond (SUPERADMIN, BACKOFFICE)

### Repository

**File:** `UserPlafondRepository.java`

**Methods:**

1. `findFirstByUserAndIsActiveTrueOrderByAssignedAtDesc(user)` - Plafond terbaru aktif
2. `findByUserAndIsActive(user, isActive)` - Plafond berdasarkan status

---

## Security

### Authorization

- **SUPERADMIN**: Full access (assign, view)
- **BACKOFFICE**: View only (get active plafond)
- **CUSTOMER**: Tidak ada akses ke endpoint ini (nanti bisa melihat plafond sendiri via profil)

### Validation

- Semua input divalidasi dengan Bean Validation
- User dan plafond existence check
- Max amount boundary check
- Transactional untuk atomicity

---

## Future Enhancements

### Akan Dibuat:

1. **Loan Application**: Penggunaan plafond saat customer ajukan pinjaman
2. **Customer View Own Plafond**: Endpoint untuk customer melihat plafond sendiri
3. **Plafond History**: Endpoint untuk melihat histori plafond user
4. **Plafond Expiration**: Tambah field expiry date untuk plafond
5. **Auto Upgrade**: Logic auto-upgrade plafond based on payment history

---

**Dokumentasi dibuat pada:** 10 Januari 2026  
**Project:** LoanOva - Loan Management System  
**Feature:** User Plafond Assignment
