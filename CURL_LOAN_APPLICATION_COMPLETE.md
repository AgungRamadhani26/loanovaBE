# Dokumentasi CURL - Loan Application API

Dokumentasi lengkap untuk semua endpoint Loan Application dengan contoh CURL.

## Base URL

```
http://localhost:9091/api/loan-applications
```

---

## 1. CUSTOMER - Submit Loan Application

**Endpoint:** `POST /api/loan-applications`  
**Role:** `CUSTOMER`  
**Content-Type:** `multipart/form-data`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--form 'branchId="1"' \
--form 'plafondId="3"' \
--form 'amount="50000000"' \
--form 'tenor="12"' \
--form 'occupation="Software Engineer"' \
--form 'companyName="PT. Nusa Indah Tech"' \
--form 'rekeningNumber="12345467"' \
--form 'savingBookCover=@"/path/to/FormAplikasiPengajuan1.jpg"' \
--form 'payslipPhoto=@"/path/to/BuktiBayarTanda.Jadi3.jpg"'
```

**Note:** Customer **memilih branch** saat submit loan application. Customer tidak di-assign ke branch tertentu, sehingga bebas mengajukan pinjaman di cabang mana saja.

### Response (201 Created)

```json
{
  "success": true,
  "message": "Pengajuan pinjaman berhasil disubmit",
  "data": {
    "id": 1,
    "userId": 2,
    "username": "customer01",
    "branchId": 1,
    "branchName": "Jakarta Pusat",
    "plafondId": 3,
    "plafondName": "Personal Loan Silver",
    "amount": 50000000,
    "tenor": 12,
    "status": "PENDING_REVIEW",
    "submittedAt": "2026-01-12T14:30:00",
    "fullNameSnapshot": "Customer One",
    "phoneNumberSnapshot": "081234567890",
    "userAddressSnapshot": "Jl. Sudirman No. 123",
    "nikSnapshot": "3201234567890123",
    "birthDateSnapshot": "1990-05-15",
    "npwpNumberSnapshot": "12.345.678.9-012.000",
    "occupation": "Software Engineer",
    "companyName": "PT. Nusa Indah Tech",
    "rekeningNumber": "12345467",
    "ktpPhotoSnapshot": "loan-snapshots/ktp_snapshot_abc123.jpg",
    "npwpPhotoSnapshot": "loan-snapshots/npwp_snapshot_def456.jpg",
    "savingBookCover": "loan-documents/saving_book_ghi789.jpg",
    "payslipPhoto": "loan-documents/payslip_jkl012.jpg"
  }
}
```

### Error Response (400 Bad Request)

```json
{
  "success": false,
  "message": "Validasi gagal",
  "data": {
    "errors": {
      "plafondId": "Plafond ID wajib diisi",
      "amount": "Jumlah pinjaman wajib diisi",
      "savingBookCover": "Foto cover buku tabungan wajib diunggah"
    }
  }
}
```

### Business Logic Validations:

- ✅ Branch ID harus valid (branch exists)
- ✅ User profile harus sudah lengkap
- ✅ Tidak boleh ada pengajuan aktif (status pending/waiting)
- ✅ Plafond ID harus sesuai dengan plafond aktif user
- ✅ Amount tidak boleh melebihi remaining amount
- ✅ Tenor harus dalam range min-max plafond
- ✅ Format numeric (plafondId, amount, tenor) harus valid

---

## 2. CUSTOMER - Get My Applications

**Endpoint:** `GET /api/loan-applications/my`  
**Role:** `CUSTOMER`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/my' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil data pengajuan pinjaman",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "customer01",
      "branchId": 1,
      "branchName": "Jakarta Pusat",
      "plafondId": 3,
      "plafondName": "Personal Loan Silver",
      "amount": 50000000,
      "tenor": 12,
      "status": "PENDING_REVIEW",
      "submittedAt": "2026-01-12T14:30:00",
      "fullNameSnapshot": "Customer One",
      "phoneNumberSnapshot": "081234567890",
      "occupation": "Software Engineer",
      "companyName": "PT. Nusa Indah Tech"
    }
  ]
}
```

---

## 3. GET Application Detail by ID

**Endpoint:** `GET /api/loan-applications/{id}`  
**Role:** `CUSTOMER`, `MARKETING`, `BRANCH_MANAGER`, `BACKOFFICE`, `SUPERADMIN`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/1' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil detail pengajuan pinjaman",
  "data": {
    "id": 1,
    "userId": 2,
    "username": "customer01",
    "branchId": 1,
    "branchName": "Jakarta Pusat",
    "plafondId": 3,
    "plafondName": "Personal Loan Silver",
    "amount": 50000000,
    "tenor": 12,
    "status": "PENDING_REVIEW",
    "submittedAt": "2026-01-12T14:30:00",
    "fullNameSnapshot": "Customer One",
    "phoneNumberSnapshot": "081234567890",
    "userAddressSnapshot": "Jl. Sudirman No. 123",
    "nikSnapshot": "3201234567890123",
    "birthDateSnapshot": "1990-05-15",
    "npwpNumberSnapshot": "12.345.678.9-012.000",
    "occupation": "Software Engineer",
    "companyName": "PT. Nusa Indah Tech",
    "rekeningNumber": "12345467",
    "ktpPhotoSnapshot": "loan-snapshots/ktp_snapshot_abc123.jpg",
    "npwpPhotoSnapshot": "loan-snapshots/npwp_snapshot_def456.jpg",
    "savingBookCover": "loan-documents/saving_book_ghi789.jpg",
    "payslipPhoto": "loan-documents/payslip_jkl012.jpg"
  }
}
```

### Business Logic:

- ✅ CUSTOMER hanya bisa lihat aplikasi miliknya sendiri
- ✅ MARKETING hanya bisa lihat aplikasi dari branch yang sama
- ✅ BRANCH_MANAGER hanya bisa lihat aplikasi dari branch yang sama
- ✅ BACKOFFICE dan SUPERADMIN bisa lihat semua aplikasi

---

## 4. GET Application History

**Endpoint:** `GET /api/loan-applications/{id}/history`  
**Role:** `ALL ROLES`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/1/history' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil history pengajuan pinjaman",
  "data": [
    {
      "id": 1,
      "loanApplicationId": 1,
      "actionByUserId": 2,
      "actionByUsername": "customer01",
      "actionByRole": "CUSTOMER",
      "status": "PENDING_REVIEW",
      "comment": "Pengajuan pinjaman berhasil disubmit",
      "createdAt": "2026-01-12T14:30:00"
    },
    {
      "id": 2,
      "loanApplicationId": 1,
      "actionByUserId": 3,
      "actionByUsername": "marketing01",
      "actionByRole": "MARKETING",
      "status": "WAITING_APPROVAL",
      "comment": "Dokumen lengkap, proceed ke approval",
      "createdAt": "2026-01-12T15:00:00"
    }
  ]
}
```

---

## 5. MARKETING - Get Pending Review Applications

**Endpoint:** `GET /api/loan-applications/pending-review`  
**Role:** `MARKETING`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/pending-review' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan pending review",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "customer01",
      "branchId": 1,
      "branchName": "Jakarta Pusat",
      "plafondId": 3,
      "plafondName": "Personal Loan Silver",
      "amount": 50000000,
      "tenor": 12,
      "status": "PENDING_REVIEW",
      "submittedAt": "2026-01-12T14:30:00",
      "fullNameSnapshot": "Customer One",
      "occupation": "Software Engineer"
    }
  ]
}
```

### Business Logic:

- ✅ Hanya menampilkan aplikasi dengan status `PENDING_REVIEW`
- ✅ Hanya dari branch yang sama dengan marketing yang login

---

## 6. MARKETING - Review Application (PROCEED/REJECT)

**Endpoint:** `PUT /api/loan-applications/{id}/review`  
**Role:** `MARKETING`  
**Content-Type:** `application/json`

### Request - PROCEED

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data '{
  "action": "PROCEED",
  "comment": "Dokumen lengkap, proceed ke approval"
}'
```

### Request - REJECT

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/review' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT",
  "comment": "Dokumen tidak lengkap, slip gaji tidak valid"
}'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Review berhasil diproses",
  "data": {
    "id": 1,
    "status": "WAITING_APPROVAL",
    "submittedAt": "2026-01-12T14:30:00"
  }
}
```

### Business Logic:

- ✅ Status harus `PENDING_REVIEW`
- ✅ Marketing harus dari branch yang sama dengan aplikasi
- ✅ Action `PROCEED` → status jadi `WAITING_APPROVAL`
- ✅ Action `REJECT` → status jadi `REJECTED_BY_MARKETING`, remaining amount dikembalikan

---

## 7. BRANCH_MANAGER - Get Waiting Approval Applications

**Endpoint:** `GET /api/loan-applications/waiting-approval`  
**Role:** `BRANCH_MANAGER`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-approval' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan waiting approval",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "customer01",
      "branchId": 1,
      "branchName": "Jakarta Pusat",
      "amount": 50000000,
      "tenor": 12,
      "status": "WAITING_APPROVAL",
      "submittedAt": "2026-01-12T14:30:00"
    }
  ]
}
```

### Business Logic:

- ✅ Hanya menampilkan aplikasi dengan status `WAITING_APPROVAL`
- ✅ Hanya dari branch yang sama dengan branch manager yang login

---

## 8. BRANCH_MANAGER - Approve Application (APPROVE/REJECT)

**Endpoint:** `PUT /api/loan-applications/{id}/approve`  
**Role:** `BRANCH_MANAGER`  
**Content-Type:** `application/json`

### Request - APPROVE

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data '{
  "action": "APPROVE",
  "comment": "Disetujui, silakan proses pencairan"
}'
```

### Request - REJECT

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/approve' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data '{
  "action": "REJECT",
  "comment": "Ditolak karena tidak memenuhi kriteria"
}'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Approval berhasil diproses",
  "data": {
    "id": 1,
    "status": "WAITING_DISBURSEMENT",
    "submittedAt": "2026-01-12T14:30:00"
  }
}
```

### Business Logic:

- ✅ Status harus `WAITING_APPROVAL`
- ✅ Branch Manager harus dari branch yang sama dengan aplikasi
- ✅ Action `APPROVE` → status jadi `WAITING_DISBURSEMENT`
- ✅ Action `REJECT` → status jadi `REJECTED_BY_BRANCH_MANAGER`, remaining amount dikembalikan

---

## 9. BACKOFFICE - Get Waiting Disbursement Applications

**Endpoint:** `GET /api/loan-applications/waiting-disbursement`  
**Role:** `BACKOFFICE`, `SUPERADMIN`

### Request

```bash
curl --location 'http://localhost:9091/api/loan-applications/waiting-disbursement' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Berhasil mengambil daftar pengajuan waiting disbursement",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "customer01",
      "branchId": 1,
      "branchName": "Jakarta Pusat",
      "amount": 50000000,
      "tenor": 12,
      "status": "WAITING_DISBURSEMENT",
      "submittedAt": "2026-01-12T14:30:00",
      "rekeningNumber": "12345467"
    }
  ]
}
```

### Business Logic:

- ✅ Menampilkan semua aplikasi dengan status `WAITING_DISBURSEMENT`
- ✅ Tidak ada filter branch (BACKOFFICE bisa lihat semua branch)

---

## 10. BACKOFFICE - Disburse Application

**Endpoint:** `PUT /api/loan-applications/{id}/disburse`  
**Role:** `BACKOFFICE`, `SUPERADMIN`

### Request

```bash
curl --location --request PUT 'http://localhost:9091/api/loan-applications/1/disburse' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "Pinjaman berhasil dicairkan",
  "data": {
    "id": 1,
    "userId": 2,
    "username": "customer01",
    "amount": 50000000,
    "tenor": 12,
    "status": "DISBURSED",
    "submittedAt": "2026-01-12T14:30:00",
    "rekeningNumber": "12345467"
  }
}
```

### Business Logic:

- ✅ Status harus `WAITING_DISBURSEMENT`
- ✅ Status akan berubah menjadi `DISBURSED`
- ✅ History akan dicatat

---

## Status Flow Diagram

```
PENDING_REVIEW (Customer submit)
    ↓ (Marketing PROCEED)
WAITING_APPROVAL
    ↓ (Branch Manager APPROVE)
WAITING_DISBURSEMENT
    ↓ (Backoffice disburse)
DISBURSED

Rejection dapat terjadi di:
- REJECTED_BY_MARKETING (dari PENDING_REVIEW)
- REJECTED_BY_BRANCH_MANAGER (dari WAITING_APPROVAL)
```

---

## Error Codes

| Status Code | Description                                           |
| ----------- | ----------------------------------------------------- |
| 200         | OK - Request berhasil                                 |
| 201         | Created - Loan application berhasil dibuat            |
| 400         | Bad Request - Validasi gagal atau business rule error |
| 401         | Unauthorized - Token tidak valid atau expired         |
| 403         | Forbidden - Role tidak memiliki akses                 |
| 404         | Not Found - Resource tidak ditemukan                  |
| 500         | Internal Server Error - Error server                  |

---

## Common Error Responses

### User Profile Belum Lengkap

```json
{
  "success": false,
  "message": "Anda belum melengkapi profil. Silakan lengkapi profil terlebih dahulu",
  "data": null
}
```

### Masih Ada Pengajuan Aktif

```json
{
  "success": false,
  "message": "Anda masih memiliki pengajuan pinjaman yang sedang diproses. Silakan tunggu hingga proses selesai sebelum mengajukan pinjaman baru",
  "data": null
}
```

### Amount Melebihi Plafond

```json
{
  "success": false,
  "message": "Jumlah pinjaman (50000000) melebihi sisa plafond Anda (30000000)",
  "data": null
}
```

### Tenor Tidak Valid

```json
{
  "success": false,
  "message": "Tenor harus antara 6 - 24 bulan untuk plafond Personal Loan Silver",
  "data": null
}
```

### Tidak Ada Plafond Aktif

```json
{
  "success": false,
  "message": "Anda belum memiliki plafond aktif. Silakan hubungi marketing agar dibantu proses plafond",
  "data": null
}
```

---

## Testing Flow Recommendation

### Flow Lengkap (Happy Path):

1. **Customer** - Submit loan application (`POST /api/loan-applications`)
2. **Customer** - Cek status (`GET /api/loan-applications/my`)
3. **Marketing** - Lihat pending review (`GET /api/loan-applications/pending-review`)
4. **Marketing** - Review PROCEED (`PUT /api/loan-applications/{id}/review`)
5. **Branch Manager** - Lihat waiting approval (`GET /api/loan-applications/waiting-approval`)
6. **Branch Manager** - Approve (`PUT /api/loan-applications/{id}/approve`)
7. **Backoffice** - Lihat waiting disbursement (`GET /api/loan-applications/waiting-disbursement`)
8. **Backoffice** - Disburse (`PUT /api/loan-applications/{id}/disburse`)
9. **Customer** - Cek history (`GET /api/loan-applications/{id}/history`)

### Test Rejection:

- Marketing REJECT di step 4
- Branch Manager REJECT di step 6
- Verifikasi remaining amount dikembalikan

---

## Notes

1. **JWT Token**: Semua endpoint memerlukan JWT token di header Authorization
2. **Multipart Form**: Submit loan application menggunakan `multipart/form-data`
3. **JSON Body**: Review dan approve menggunakan `application/json`
4. **File Upload**: Maksimal 10MB per file, total request 30MB
5. **Role-Based Access**: Setiap endpoint memiliki role restriction
6. **Branch Selection**: Customer **memilih branch** saat submit loan application (branchId required)
7. **Branch Filter**: Marketing dan Branch Manager hanya bisa akses aplikasi dari branch sendiri
8. **Field Count**: Total 9 fields (7 text + 2 files) untuk menghindari Tomcat limit

---

**Last Updated:** 2026-01-12  
**API Version:** 1.0  
**Base URL:** http://localhost:9091
