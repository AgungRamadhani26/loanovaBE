# Loanova API Documentation

Dokumentasi lengkap REST API untuk aplikasi backend Loanova (`loanova`).

**Base URL**: `http://localhost:9091` (Default)

---

## 1. Authentication
**Base Path**: `/api/auth`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/register` | Mendaftarkan customer baru. | Public |
| `POST` | `/login` | Login user (semua role). | Public |
| `POST` | `/firebase-google` | Login menggunakan Google via Firebase Authentication. | Public |
| `POST` | `/refresh` | Refresh access token menggunakan refresh token. | Public |
| `POST` | `/logout` | Logout user dan invalidsi token. | Authenticated |
| `POST` | `/forgot-password` | Request link reset password ke email. | Public |
| `POST` | `/reset-password` | Reset password menggunakan token dari email. | Public |
| `POST` | `/change-password` | Mengganti password saat user sedang login. | Authenticated |

### CURL Examples

#### Login
```bash
curl -X POST http://localhost:9091/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Bima12345@",
    "password": "Bima12345*"
  }'
```

#### Change Password
```bash
curl -X POST http://localhost:9091/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "currentPassword": "OldPassword123!",
    "newPassword": "NewPassword456!"
  }'
```

**Response Success:**
```json
{
  "success": true,
  "message": "Password berhasil diubah. Silakan login kembali.",
  "data": null,
  "code": 200,
  "timestamp": "2026-01-31T05:04:48.453476600Z"
}
```

**Response Error (Validation):**
```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "newPassword": "Password baru wajib diisi",
      "currentPassword": "Password lama wajib diisi"
    }
  },
  "code": 400,
  "timestamp": "2026-01-31T05:04:48.453476600Z"
}
```

#### Firebase Google Login
```bash
curl -X POST http://localhost:9091/api/auth/firebase-google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU...",
    "fcmToken": "dGhpc19pc19hX2ZjbV90b2tlbg..."
  }'
```

**Response Success:**
```json
{
  "success": true,
  "message": "Login dengan Google berhasil",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "type": "Bearer",
    "username": "user@gmail.com",
    "roles": ["CUSTOMER"]
  },
  "code": 200,
  "timestamp": "2026-02-05T06:38:22.123456789Z"
}
```

**Flow:**
1. Android app performs Google Sign-In via Firebase
2. Android app receives Firebase ID Token
3. Android app sends ID Token to this endpoint
4. Backend verifies token with Firebase Admin SDK
5. Backend creates/links user account and returns JWT tokens

---

## 2. User Management
### Users
**Base Path**: `/api/users`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Mengambil daftar semua user. | `USER:READ` (Superadmin) |
| `POST` | `/` | Membuat user internal baru. | `USER:CREATE` (Superadmin) |
| `GET` | `/{id}` | Mengambil detail user berdasarkan ID. | `USER:DETAILS` (Superadmin) |
| `GET` | `/by-username/{username}` | Mengambil detail user berdasarkan username (profil sendiri). | Authenticated (Own Profile) |
| `PUT` | `/{id}` | Memperbarui data user. | `USER:UPDATE` (Superadmin) |
| `DELETE` | `/{id}` | Menghapus user. | `USER:DELETE` (Superadmin) |

### CURL Examples

#### Get User by Username (Own Profile)
```bash
curl -X GET http://localhost:9091/api/users/by-username/Bima12345@ \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Berhasil mengambil data pengguna",
  "data": {
    "id": 2,
    "username": "Bima12345@",
    "email": "bima@gmail.com",
    "branchCode": null,
    "roles": ["SUPERADMIN", "BACKOFFICE"],
    "isActive": true
  },
  "code": 200,
  "timestamp": "2026-01-31T06:11:21.123456789Z"
}
```

### Profiles (Customer)
**Base Path**: `/api/user-profiles`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/me` | Mengambil profil user yang sedang login. | `PROFILE:READ_MY` (Customer) |
| `POST` | `/complete` | Melengkapi data profil (Multipart). | `PROFILE:COMPLETE` (Customer) |
| `PUT` | `/update` | Memperbarui data profil (Multipart). | `PROFILE:UPDATE` (Customer) |

### Roles & Permissions
**Base Path**: `/api/roles` & `/api/permissions`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/roles` | Daftar semua role. | `ROLE:READ` (Superadmin) |
| `POST` | `/api/roles` | Membuat role custom baru. | `ROLE:CREATE` (Superadmin) |
| `PUT` | `/api/roles/{id}` | Update role & permissions. | `ROLE:UPDATE` (Superadmin) |
| `DELETE` | `/api/roles/{id}` | Hapus role. | `ROLE:DELETE` (Superadmin) |
| `GET` | `/api/permissions` | Daftar semua permission sistem. | `PERMISSION:READ` (Superadmin) |

### User Plafond (Assignment)
**Base Path**: `/api/user-plafonds`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/assign` | Assign plafond ke user (Customer). | `USER_PLAFOND:ASSIGN` (Admin) |
| `GET` | `/users/{userId}/active` | Get plafond aktif user. | `USER_PLAFOND:READ` (Admin) |
| `GET` | `/users/{userId}/history` | Get riwayat plafond user. | `USER_PLAFOND:HISTORY` (Admin) |

---

## 3. Loan Management
### Loan Applications
**Base Path**: `/api/loan-applications`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/` | Submit pengajuan pinjaman baru. | `LOAN:SUBMIT` (Customer) |
| `GET` | `/` | Get semua pengajuan (Filter by role). | `LOAN:READ_ALL` (All Roles) |
| `GET` | `/my` | Get pengajuan saya sendiri. | `LOAN:READ_MY` (Customer) |
| `GET` | `/{id}` | Get detail pengajuan. | `LOAN:DETAILS` (All Roles) |
| `GET` | `/{id}/history` | Get history status pengajuan. | `LOAN:HISTORY` (All Roles) |
| `GET` | `/pending-review` | Get list pending review (Marketing). | `LOAN:LIST_PENDING_REVIEW` |
| `PUT` | `/{id}/review` | Review (Proceed/Reject) pengajuan. | `LOAN:REVIEW` (Marketing) |
| `GET` | `/waiting-approval` | Get list waiting approval (Branch Manager). | `LOAN:LIST_WAITING_APPROVAL` |
| `PUT` | `/{id}/approve` | Approve/Reject pengajuan. | `LOAN:APPROVE` (Branch Manager) |
| `GET` | `/waiting-disbursement` | Get list waiting disbursement (Backoffice). | `LOAN:LIST_WAITING_DISBURSE` |
| `PUT` | `/{id}/disburse` | Cairkan dana pinjaman (Disburse). | `LOAN:DISBURSE` (Backoffice) |
| `PUT` | `/{id}/backoffice-reject` | Tolak pencairan. | `LOAN:REJECT_BACKOFFICE` |

### Plafond Master Data
**Base Path**: `/api/plafonds`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/public` | Daftar plafond untuk umum. | Public |
| `GET` | `/` | Daftar semua plafond (Admin). | `PLAFOND:READ` (Superadmin) |
| `POST` | `/` | Buat jenis plafond baru. | `PLAFOND:CREATE` (Superadmin) |
| `GET` | `/{id}` | Detail plafond. | `PLAFOND:DETAILS` (Superadmin) |
| `DELETE` | `/{id}` | Hapus (Soft Delete) plafond. | `PLAFOND:DELETE` (Superadmin) |

---

## 4. Branch Management
**Base Path**: `/api/branches`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Daftar semua cabang. | `BRANCH:READ` (All Roles) |
| `POST` | `/` | Tambah cabang baru. | `BRANCH:CREATE` (Superadmin) |
| `PUT` | `/{id}` | Update data cabang. | `BRANCH:UPDATE` (Superadmin) |
| `DELETE` | `/{id}` | Hapus cabang. | `BRANCH:DELETE` (Superadmin) |
| `PUT` | `/{id}/restore` | Restore cabang yang dihapus. | `BRANCH:RESTORE` (Superadmin) |

---

## 5. Notifications
**Base Path**: `/api/notifications`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Daftar notifikasi saya. | Authenticated |
| `PUT` | `/{id}/read` | Tandai notifikasi sudah dibaca. | Authenticated |
| `PUT` | `/read-all` | Tandai semua notifikasi sudah dibaca. | Authenticated |
| `POST` | `/test-push` | Test kirim notifikasi (Superadmin). | `ROLE_SUPERADMIN` |

---

## 6. Dashboard Statistics
**Base Path**: `/api/dashboard`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/statistics` | Statistik dashboard (role-based filtered). | `DASHBOARD:STATISTICS` |

### CURL Example

```bash
curl -X GET http://localhost:9091/api/dashboard/statistics \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Response Success

```json
{
  "success": true,
  "message": "Berhasil mengambil statistik dashboard",
  "data": {
    "totalSubmissionAmount": 50000000.00,
    "totalDisbursedAmount": 35000000.00,
    "estimatedPrincipal": 35000000.00,
    "estimatedInterest": 4200000.00,
    "estimatedTotalIncome": 39200000.00,
    "statusDistribution": [
      { "status": "PENDING_REVIEW", "count": 5, "percentage": 25.0 },
      { "status": "WAITING_APPROVAL", "count": 3, "percentage": 15.0 },
      { "status": "WAITING_DISBURSEMENT", "count": 2, "percentage": 10.0 },
      { "status": "DISBURSED", "count": 8, "percentage": 40.0 },
      { "status": "REJECTED", "count": 2, "percentage": 10.0 }
    ],
    "plafondDistribution": [
      { "plafondName": "Gold", "count": 15 },
      { "plafondName": "Silver", "count": 12 },
      { "plafondName": "Bronze", "count": 8 }
    ]
  },
  "code": 200,
  "timestamp": "2026-02-07T10:00:00.000000000Z"
}
```

### Notes
- **SUPERADMIN & BACKOFFICE**: Melihat statistik dari **semua data** di seluruh cabang.
- **MARKETING & BRANCHMANAGER**: Melihat statistik hanya dari **data cabang sendiri**.

---

## Response Format

Semua API mengembalikan format standar:

```json
{
  "success": true,
  "message": "Pesan dari server",
  "data": { ... },
  "code": 200,
  "timestamp": "2026-01-31T06:11:21.123456789Z"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Pesan error",
  "data": {
    "errors": {
      "fieldName": "Pesan validasi field"
    }
  },
  "code": 400,
  "timestamp": "2026-01-31T06:11:21.123456789Z"
}
```

---
*Generated by Loanova Assistant - Last Updated: 2026-02-07*

