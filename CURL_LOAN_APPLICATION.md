# cURL EXAMPLES - LOAN APPLICATION API

## Prerequisites

1. Login sebagai user dengan role yang sesuai
2. Dapatkan access token dari endpoint `/api/auth/login`
3. Gunakan token dalam header `Authorization: Bearer <token>`
4. Customer harus sudah **melengkapi user profile** terlebih dahulu

---

## 🔑 Quick Login Examples

### Login as Customer

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "customer1",
  "password": "password123"
}'
```

### Login as Marketing

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "marketing1",
  "password": "password123"
}'
```

### Login as Branch Manager

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "branchmanager1",
  "password": "password123"
}'
```

### Login as Backoffice

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "backoffice1",
  "password": "password123"
}'
```

---

## 1. CUSTOMER - Submit Loan Application

**Endpoint:** `POST /api/loan-applications`  
**Role:** CUSTOMER
**Content-Type:** multipart/form-data

### Windows (PowerShell)

```powershell
$headers = @{
    "Authorization" = "Bearer <CUSTOMER_TOKEN>"
}

$form = @{
    plafondId = "3"
    amount = "5000000"
    tenor = "12"
    occupation = "Software Engineer"
    companyName = "PT Tech Indonesia"
    rekeningNumber = "1234567890"
    savingBookCover = Get-Item -Path "C:\path\to\saving_book.jpg"
    payslipPhoto = Get-Item -Path "C:\path\to\payslip.jpg"
    spouseKtpPhoto = Get-Item -Path "C:\path\to\spouse_ktp.jpg"
    marriageCertificatePhoto = Get-Item -Path "C:\path\to\marriage_cert.jpg"
    employeeIdPhoto = Get-Item -Path "C:\path\to\employee_id.jpg"
}

Invoke-RestMethod -Uri "http://localhost:9091/api/loan-applications" `
    -Method Post `
    -Headers $headers `
    -Form $form
```

### Linux/Mac (cURL)

```bash
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'plafondId="3"' \
--form 'amount="5000000"' \
--form 'tenor="12"' \
--form 'occupation="Software Engineer"' \
--form 'companyName="PT Tech Indonesia"' \
--form 'rekeningNumber="1234567890"' \
--form 'savingBookCover=@"/path/to/saving_book.jpg"' \
--form 'payslipPhoto=@"/path/to/payslip.jpg"' \
--form 'spouseKtpPhoto=@"/path/to/spouse_ktp.jpg"' \
--form 'marriageCertificatePhoto=@"/path/to/marriage_cert.jpg"' \
--form 'employeeIdPhoto=@"/path/to/employee_id.jpg"'
```

**Notes:**

- ✅ Foto KTP & NPWP akan di-**snapshot** (copy) ke folder `loan-snapshots/` untuk isolasi data
- ✅ Dokumen baru (saving book, payslip, dll) disimpan di folder `loan-documents/`
- ✅ Perubahan foto di user profile **tidak mempengaruhi** snapshot loan
- ✅ `spouseKtpPhoto`, `marriageCertificatePhoto`, `employeeIdPhoto` adalah **optional**

**Expected Response (201 Created):**

```json
{
  "success": true,
  "message": "Pengajuan pinjaman berhasil disubmit",
  "data": {
    "id": 1,
    "userId": 5,
    "username": "customer1",
    "branchId": 1,
    "branchCode": "BCA001",
    "plafondId": 3,
    "plafondName": "Bronze",
    "amount": 5000000,
    "tenor": 12,
    "status": "PENDING_REVIEW",
    "submittedAt": "2024-01-15T10:30:00",
    "fullNameSnapshot": "John Doe",
    "ktpPhotoSnapshot": "/uploads/loan-snapshots/ktp_abc-123-def.jpg",
    "npwpPhotoSnapshot": "/uploads/loan-snapshots/npwp_xyz-456-ghi.jpg",
    "savingBookCover": "/uploads/loan-documents/saving_book_1234.jpg",
    "payslipPhoto": "/uploads/loan-documents/payslip_5678.jpg"
  },
  "code": 201,
  "timestamp": "2026-01-11T10:30:00Z"
}
```

---

## 2. CUSTOMER - Get My Loan Applications

**Endpoint:** `GET /api/loan-applications/my`  
**Role:** CUSTOMER

```bash
curl --location 'http://localhost:9091/api/loan-applications/my' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil data pengajuan pinjaman",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "customer1",
      "branchId": 1,
      "branchCode": "BCA001",
      "plafondId": 3,
      "plafondName": "Bronze",
      "amount": 5000000,
      "tenor": 12,
      "status": "PENDING_REVIEW",
      "submittedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "userId": 5,
      "username": "customer1",
      "amount": 3000000,
      "tenor": 6,
      "status": "DISBURSED",
      "submittedAt": "2024-01-10T09:00:00"
    }
  ],
  "code": 200,
  "timestamp": "2026-01-11T10:35:00Z"
}
```

---

## 3. Get Loan Application Detail

**Endpoint:** `GET /api/loan-applications/{id}`  
**Role:** CUSTOMER, MARKETING, BRANCH_MANAGER, BACKOFFICE, SUPERADMIN  
**Note:** Customer hanya bisa lihat aplikasi miliknya sendiri

```bash
curl --location 'http://localhost:9091/api/loan-applications/1' \
--header 'Authorization: Bearer <TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil detail pengajuan pinjaman",
  "data": {
    "id": 1,
    "userId": 5,
    "username": "customer1",
    "branchId": 1,
    "branchCode": "BCA001",
    "plafondId": 3,
    "plafondName": "Bronze",
    "amount": 5000000,
    "tenor": 12,
    "status": "PENDING_REVIEW",
    "submittedAt": "2024-01-15T10:30:00",
    "fullNameSnapshot": "John Doe",
    "phoneNumberSnapshot": "081234567890",
    "userAddressSnapshot": "Jl. Sudirman No. 123, Jakarta",
    "nikSnapshot": "3171234567890123",
    "birthDateSnapshot": "1990-05-15",
    "npwpNumberSnapshot": "123456789012345",
    "occupation": "Software Engineer",
    "companyName": "PT Tech Indonesia",
    "rekeningNumber": "1234567890",
    "ktpPhotoSnapshot": "/uploads/loan-snapshots/ktp_abc-123.jpg",
    "npwpPhotoSnapshot": "/uploads/loan-snapshots/npwp_xyz-456.jpg",
    "savingBookCover": "/uploads/loan-documents/saving_book_1234.jpg",
    "payslipPhoto": "/uploads/loan-documents/payslip_5678.jpg",
    "spouseKtpPhoto": "/uploads/loan-documents/spouse_ktp_9012.jpg",
    "marriageCertificatePhoto": null,
    "employeeIdPhoto": "/uploads/loan-documents/employee_id_3456.jpg"
  },
  "code": 200,
  "timestamp": "2026-01-11T10:40:00Z"
}
```

**Note:** Perhatikan `ktpPhotoSnapshot` dan `npwpPhotoSnapshot` berada di folder `loan-snapshots/` (bukan folder asli `ktp/` atau `npwp/`)

---

## 4. Get Application History

**Endpoint:** `GET /api/loan-applications/{id}/history`  
**Role:** CUSTOMER, MARKETING, BRANCH_MANAGER, BACKOFFICE, SUPERADMIN

```bash
curl --location 'http://localhost:9091/api/loan-applications/1/history' \
--header 'Authorization: Bearer <TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil history pengajuan pinjaman",
  "data": [
    {
      "id": 3,
      "loanApplicationId": 1,
      "actionByUserId": 3,
      "actionByUsername": "marketing1",
      "actionByRole": "MARKETING",
      "status": "WAITING_APPROVAL",
      "comment": "Dokumen lengkap, silakan diproses",
      "createdAt": "2024-01-15T11:00:00"
    },
    {
      "id": 2,
      "loanApplicationId": 1,
      "actionByUserId": 5,
      "actionByUsername": "customer1",
      "actionByRole": "CUSTOMER",
      "status": "PENDING_REVIEW",
      "comment": "Pengajuan pinjaman berhasil disubmit",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "code": 200,
  "timestamp": "2026-01-11T11:00:00Z"
}
```

---

## 5. MARKETING - Get Pending Applications

**Endpoint:** `GET /api/loan-applications/pending-review`  
**Role:** MARKETING  
**Note:** Hanya menampilkan aplikasi dari **branch marketing** tersebut

```bash
curl --location 'http://localhost:9091/api/loan-applications/pending-review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan pending review",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "customer1",
      "branchId": 1,
      "branchCode": "BCA001",
      "amount": 5000000,
      "tenor": 12,
      "status": "PENDING_REVIEW",
      "submittedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "userId": 6,
      "username": "customer2",
      "amount": 3000000,
      "tenor": 6,
      "status": "PENDING_REVIEW",
      "submittedAt": "2024-01-15T11:00:00"
    }
  ],
  "code": 200,
  "timestamp": "2026-01-11T11:10:00Z"
}
```

---

## 6. MARKETING - Review Application (PROCEED)

**Endpoint:** `PUT /api/loan-applications/{id}/review`  
**Role:** MARKETING  
**Note:** Marketing hanya bisa review aplikasi di **branch-nya sendiri**

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "PROCEED",
  "comment": "Dokumen lengkap dan sesuai persyaratan"
}'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Review berhasil diproses",
  "data": {
    "id": 1,
    "userId": 5,
    "username": "customer1",
    "branchId": 1,
    "status": "WAITING_APPROVAL",
    "amount": 5000000,
    "tenor": 12
  },
  "code": 200,
  "timestamp": "2026-01-11T11:15:00Z"
}
```

---

## 7. MARKETING - Review Application (REJECT)

**Endpoint:** `PUT /api/loan-applications/{id}/review`  
**Role:** MARKETING  
**Note:** Comment **wajib** diisi saat REJECT

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT",
  "comment": "Dokumen tidak lengkap, silakan upload ulang payslip terbaru"
}'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Review berhasil diproses",
  "data": {
    "id": 1,
    "status": "REJECTED",
    "amount": 5000000,
    "tenor": 12
  },
  "code": 200,
  "timestamp": "2026-01-11T11:20:00Z"
}
```

**⚠️ Important:** Remaining amount akan **dikembalikan** ke user plafond

---

## 8. BRANCH_MANAGER - Get Waiting Approval Applications

**Endpoint:** `GET /api/loan-applications/waiting-approval`  
**Role:** BRANCH_MANAGER  
**Note:** Hanya menampilkan aplikasi dari **branch branch manager** tersebut

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-approval' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "statusCode": 200,
  "message": "Berhasil mengambil daftar pengajuan waiting approval",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "customer1",
      "branchId": 1,
      "branchCode": "BCA001",
      "amount": 5000000,
      "tenor": 12,
      "status": "WAITING_APPROVAL",
      "submittedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

## 9. BRANCH_MANAGER - Approve Application (APPROVE)

````bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>' \
```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan waiting approval",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "customer1",
      "branchId": 1,
      "branchCode": "BCA001",
      "amount": 5000000,
      "tenor": 12,
      "status": "WAITING_APPROVAL",
      "submittedAt": "2024-01-15T10:30:00"
    }
  ],
  "code": 200,
  "timestamp": "2026-01-11T11:25:00Z"
}
````

---

## 9. BRANCH_MANAGER - Approve Application (APPROVE)

**Endpoint:** `PUT /api/loan-applications/{id}/approve`  
**Role:** BRANCH_MANAGER  
**Note:** Branch Manager hanya bisa approve aplikasi di **branch-nya sendiri**

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "APPROVE",
  "comment": "Pinjaman disetujui, silakan proses pencairan"
}'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Approval berhasil diproses",
  "data": {
    "id": 1,
    "status": "WAITING_DISBURSEMENT",
    "amount": 5000000,
    "tenor": 12
  },
  "code": 200,
  "timestamp": "2026-01-11T11:30:00Z"
}
```

---

## 10. BRANCH_MANAGER - Approve Application (REJECT)

**Endpoint:** `PUT /api/loan-applications/{id}/approve`  
**Role:** BRANCH_MANAGER  
**Note:** Comment **wajib** diisi saat REJECT

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT",
  "comment": "Pinjaman ditolak karena credit scoring tidak memenuhi syarat"
}'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Approval berhasil diproses",
  "data": {
    "id": 1,
    "status": "REJECTED",
    "amount": 5000000,
    "tenor": 12
  },
  "code": 200,
  "timestamp": "2026-01-11T11:35:00Z"
}
```

**⚠️ Important:** Remaining amount akan **dikembalikan** ke user plafond

---

## 11. BACKOFFICE - Get Waiting Disbursement Applications

**Endpoint:** `GET /api/loan-applications/waiting-disbursement`  
**Role:** BACKOFFICE, SUPERADMIN  
**Note:** Backoffice bisa melihat aplikasi dari **SEMUA BRANCH**

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-disbursement' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
```

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan waiting disbursement",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "customer1",
      "branchId": 1,
      "branchCode": "BCA001",
      "amount": 5000000,
      "tenor": 12,
      "status": "WAITING_DISBURSEMENT",
      "submittedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 3,
      "userId": 8,
      "username": "customer3",
      "branchId": 2,
      "branchCode": "BCA002",
      "amount": 7000000,
      "tenor": 18,
      "status": "WAITING_DISBURSEMENT",
      "submittedAt": "2024-01-15T09:00:00"
    }
  ],
  "code": 200,
  "timestamp": "2026-01-11T11:40:00Z"
}
```

---

## 12. BACKOFFICE - Disburse Application

**Endpoint:** `PUT /api/loan-applications/{id}/disburse`  
**Role:** BACKOFFICE, SUPERADMIN

````bash
```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/disburse' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
````

**Expected Response (200 OK):**

```json
{
  "success": true,
  "message": "Pinjaman berhasil dicairkan",
  "data": {
    "id": 1,
    "status": "DISBURSED",
    "amount": 5000000,
    "tenor": 12
  },
  "code": 200,
  "timestamp": "2026-01-11T11:45:00Z"
}
```

---

## 📋 Testing Flow - Complete Scenario (Happy Path)

### Step 1: Login as Customer

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "customer1",
  "password": "password123"
}'
```

**Save the access token from response**

---

### Step 2: Complete User Profile (if not yet)

Customer harus melengkapi profile terlebih dahulu. Lihat [USER_PROFILE_TUTORIAL.md](USER_PROFILE_TUTORIAL.md)

---

### Step 3: Customer Submit Loan

```bash
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'plafondId="3"' \
--form 'amount="5000000"' \
--form 'tenor="12"' \
--form 'occupation="Software Engineer"' \
--form 'companyName="PT Tech Indonesia"' \
--form 'rekeningNumber="1234567890"' \
--form 'savingBookCover=@"C:\path\to\saving_book.jpg"' \
--form 'payslipPhoto=@"C:\path\to\payslip.jpg"'
```

**Status:** PENDING_REVIEW  
**Remaining amount berkurang**

---

### Step 4: Login as Marketing

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "marketing1",
  "password": "password123"
}'
```

---

### Step 5: Marketing Get Pending List

```bash
curl --location 'http://localhost:9091/api/loan-applications/pending-review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>'
```

---

### Step 6: Marketing Review (PROCEED)

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "PROCEED",
  "comment": "Dokumen lengkap dan sesuai"
}'
```

**Status:** WAITING_APPROVAL

---

### Step 7: Login as Branch Manager

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "branchmanager1",
  "password": "password123"
}'
```

---

### Step 8: Branch Manager Get Waiting Approval

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-approval' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>'
```

---

### Step 9: Branch Manager Approve

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer <BRANCH_MANAGER_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "APPROVE",
  "comment": "Disetujui untuk pencairan"
}'
```

**Status:** WAITING_DISBURSEMENT

---

### Step 10: Login as Backoffice

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "backoffice1",
  "password": "password123"
}'
```

---

### Step 11: Backoffice Get Waiting Disbursement

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-disbursement' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
```

---

### Step 12: Backoffice Disburse

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/disburse' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
```

**Status:** DISBURSED ✅

---

### Step 13: Check History

```bash
curl --location 'http://localhost:9091/api/loan-applications/1/history' \
--header 'Authorization: Bearer <TOKEN>'
```

**Expected history:**

1. CUSTOMER → PENDING_REVIEW
2. MARKETING → WAITING_APPROVAL
3. BRANCH_MANAGER → WAITING_DISBURSEMENT
4. BACKOFFICE → DISBURSED

---

## ⚠️ Error Examples

### Error 1: User Profile Incomplete

```bash
# Customer submit loan tanpa lengkapi profile
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Anda belum melengkapi profil. Silakan lengkapi profil terlebih dahulu",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:00:00Z"
}
```

---

### Error 2: Active Loan Exists

```bash
# Customer submit loan kedua sementara yang pertama masih pending
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Anda masih memiliki pengajuan pinjaman yang sedang diproses. Silakan tunggu hingga proses selesai sebelum mengajukan pinjaman baru",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:05:00Z"
}
```

---

### Error 3: Amount Exceeds Remaining

```bash
# Customer submit amount > remaining amount
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'amount="15000000"' \
...
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Jumlah pinjaman (15000000) melebihi sisa plafond Anda (10000000)",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:10:00Z"
}
```

---

### Error 4: Invalid Tenor

```bash
# Tenor di luar range plafond
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'tenor="60"' \
...
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Tenor harus antara 6 - 36 bulan untuk plafond Bronze",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:15:00Z"
}
```

---

### Error 5: Branch Access Denied

```bash
# Marketing Branch A coba review loan dari Branch B
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_BRANCH_A_TOKEN>' \
...
```

**Response (404 Not Found):**

```json
{
  "success": false,
  "message": "Loan application tidak ditemukan atau bukan di branch Anda",
  "data": null,
  "code": 404,
  "timestamp": "2026-01-11T12:20:00Z"
}
```

---

### Error 6: Wrong Status

```bash
# Marketing coba review loan yang sudah WAITING_APPROVAL
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Loan application tidak dalam status PENDING_REVIEW. Status saat ini: WAITING_APPROVAL",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:25:00Z"
}
```

---

### Error 7: Missing Comment on Reject

```bash
# Reject tanpa comment
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT"
}'
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "Comment wajib diisi jika melakukan reject",
  "data": null,
  "code": 400,
  "timestamp": "2026-01-11T12:30:00Z"
}
```

---

## 📝 Important Notes

### Snapshot Pattern

- ✅ Foto KTP & NPWP **di-copy** ke folder `/uploads/loan-snapshots/`
- ✅ Path snapshot disimpan di database
- ✅ Perubahan foto di user profile **tidak mempengaruhi** loan application
- ✅ Data historis tetap utuh untuk audit trail

### File Upload Structure

```
uploads/
├── ktp/                    ← User profile KTP (original)
├── npwp/                   ← User profile NPWP (original)
├── profiles/               ← User profile photos
├── loan-snapshots/         ← SNAPSHOT (copy dari user profile)
│   ├── ktp_abc-123.jpg
│   └── npwp_xyz-456.jpg
└── loan-documents/         ← Dokumen loan application
    ├── saving_book_xxx.jpg
    ├── payslip_xxx.jpg
    └── ...
```

### Remaining Amount Management

- ✅ Berkurang saat **submit** loan application
- ✅ Dikembalikan saat **reject** (by Marketing atau Branch Manager)
- ✅ **TIDAK bertambah** saat loan disbursed (no repayment tracking)

### Branch Access Control

- ✅ CUSTOMER → Lihat loan milik sendiri
- ✅ MARKETING → Proses loan di **branch sendiri**
- ✅ BRANCH_MANAGER → Proses loan di **branch sendiri**
- ✅ BACKOFFICE → Proses loan dari **SEMUA BRANCH**
- ✅ SUPERADMIN → Akses **SEMUA BRANCH**

### Status Transition Rules

```
PENDING_REVIEW (Customer submit)
    ↓ Marketing PROCEED
WAITING_APPROVAL
    ↓ Branch Manager APPROVE
WAITING_DISBURSEMENT
    ↓ Backoffice DISBURSE
DISBURSED ✅

REJECTED (dapat terjadi di stage Marketing atau Branch Manager)
```

### Validation Rules

1. User profile **harus lengkap** sebelum submit loan
2. **Tidak boleh** ada loan aktif (status: PENDING_REVIEW, WAITING_APPROVAL, WAITING_DISBURSEMENT)
3. Amount ≤ Remaining Amount
4. Tenor dalam rentang min-max plafond
5. Comment **wajib** saat reject
6. Action harus sesuai: PROCEED/REJECT (Marketing), APPROVE/REJECT (Branch Manager)

---

**Created by:** Loanova Development Team  
**Last Updated:** 2026-01-11  
**Version:** 2.0 (with Snapshot Pattern)
"action": "APPROVE",
"comment": "Disetujui"
}'

````

### Step 10: Login as Backoffice

```bash
curl --location 'http://localhost:9091/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "backoffice1",
  "password": "password123"
}'
````

### Step 11: Backoffice Get Waiting Disbursement

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-disbursement' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
```

### Step 12: Backoffice Disburse

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/disburse' \
--header 'Authorization: Bearer <BACKOFFICE_TOKEN>'
```

### Step 13: Check History

```bash
curl --location 'http://localhost:9091/api/loan-applications/1/history' \
--header 'Authorization: Bearer <TOKEN>'
```

---

## Error Examples

### Error: User Profile Incomplete

```bash
# Customer submit loan tanpa lengkapi profile
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Anda belum melengkapi profil. Silakan lengkapi profil terlebih dahulu"
}
```

---

### Error: Active Loan Exists

```bash
# Customer submit loan kedua sementara yang pertama masih pending
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Anda masih memiliki pengajuan pinjaman yang sedang diproses. Silakan tunggu hingga proses selesai sebelum mengajukan pinjaman baru"
}
```

---

### Error: Amount Exceeds Remaining

```bash
# Customer submit amount > remaining amount
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'amount="15000000"' \
...
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Jumlah pinjaman (15000000) melebihi sisa plafond Anda (10000000)"
}
```

---

### Error: Invalid Tenor

```bash
# Tenor di luar range plafond
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer <CUSTOMER_TOKEN>' \
--form 'tenor="60"' \
...
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Tenor harus antara 6 - 36 bulan untuk plafond Bronze"
}
```

---

### Error: Branch Access Denied

```bash
# Marketing Branch A coba review loan dari Branch B
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_BRANCH_A_TOKEN>' \
...
```

**Response (404 Not Found):**

```json
{
  "statusCode": 404,
  "message": "Loan application tidak ditemukan atau bukan di branch Anda"
}
```

---

### Error: Wrong Status

```bash
# Marketing coba review loan yang sudah WAITING_APPROVAL
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
...
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Loan application tidak dalam status PENDING_REVIEW. Status saat ini: WAITING_APPROVAL"
}
```

---

### Error: Missing Comment on Reject

```bash
# Reject tanpa comment
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer <MARKETING_TOKEN>' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT"
}'
```

**Response (400 Bad Request):**

```json
{
  "statusCode": 400,
  "message": "Comment wajib diisi jika melakukan reject"
}
```

---

## Notes

1. **File Upload**: Gunakan format multipart/form-data untuk submit loan application
2. **Token**: Pastikan menggunakan token yang sesuai dengan role
3. **Branch**: Marketing dan Branch Manager hanya bisa akses loan di branch mereka
4. **Status Flow**: Pastikan status sesuai sebelum melakukan action
5. **Comment**: Wajib memberikan comment saat reject
6. **Remaining Amount**: Akan berkurang saat submit, dikembalikan saat reject

---

**Created by:** Loanova Development Team  
**Last Updated:** 2024-01-15
