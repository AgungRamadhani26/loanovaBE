# DOKUMENTASI FITUR LOAN APPLICATION

## Deskripsi

Fitur Loan Application adalah sistem pengajuan pinjaman yang melibatkan 4 role: **CUSTOMER**, **MARKETING**, **BRANCH_MANAGER**, dan **BACKOFFICE**. Sistem ini mengimplementasikan workflow multi-stage approval dengan snapshot data untuk audit trail.

---

## Alur Bisnis

### 1. **Customer Submit Loan Application**

- Customer mengajukan pinjaman dengan data:
  - Plafond ID (harus sesuai dengan plafond aktif)
  - Amount (jumlah pinjaman)
  - Tenor (jangka waktu dalam bulan)
  - Occupation & Company Name
  - Rekening Number
  - Upload Documents (Saving Book Cover, Payslip, dll)
- Validasi:
  - User profile harus sudah lengkap
  - Tidak boleh ada pinjaman aktif yang sedang diproses
  - Amount tidak boleh melebihi remaining amount
  - Tenor harus dalam rentang min-max plafond
- Sistem melakukan:
  - **Snapshot data** dari user profile (nama, NIK, alamat, dll)
  - Upload dokumen ke server
  - Kurangi remaining amount di user plafond
  - Set status: **PENDING_REVIEW**
  - Catat history

### 2. **Marketing Review**

- Marketing melihat list pinjaman dengan status PENDING_REVIEW **di branch-nya**
- Marketing melakukan review dengan 2 pilihan:
  - **PROCEED**: Status berubah → **WAITING_APPROVAL**
  - **REJECT**: Status berubah → **REJECTED**, remaining amount dikembalikan
- Comment wajib diisi jika REJECT
- Catat history dengan role: MARKETING

### 3. **Branch Manager Approve**

- Branch Manager melihat list pinjaman dengan status WAITING_APPROVAL **di branch-nya**
- Branch Manager melakukan approval dengan 2 pilihan:
  - **APPROVE**: Status berubah → **WAITING_DISBURSEMENT**
  - **REJECT**: Status berubah → **REJECTED**, remaining amount dikembalikan
- Comment wajib diisi jika REJECT
- Catat history dengan role: BRANCH_MANAGER

### 4. **Backoffice Disburse**

- Backoffice melihat list pinjaman dengan status WAITING_DISBURSEMENT **dari semua branch**
- Backoffice melakukan disbursement:
  - Status berubah → **DISBURSED**
- Catat history dengan role: BACKOFFICE

---

## Status Flow

```
PENDING_REVIEW
    ↓ (Marketing PROCEED)
WAITING_APPROVAL
    ↓ (Branch Manager APPROVE)
WAITING_DISBURSEMENT
    ↓ (Backoffice DISBURSE)
DISBURSED

                ↓ (Marketing/Branch Manager REJECT)
              REJECTED
```

**Catatan:**

- Reject bisa terjadi di stage Marketing atau Branch Manager
- Jika reject, remaining amount dikembalikan ke user plafond
- Pinjaman dianggap selesai jika statusnya DISBURSED atau REJECTED

---

## Snapshot Pattern

Sistem menggunakan **snapshot pattern** untuk menyimpan data customer pada saat pengajuan. Data ini disimpan langsung di tabel `loan_applications` (tidak menggunakan relasi).

**Data yang di-snapshot:**

- fullNameSnapshot
- phoneNumberSnapshot
- userAddressSnapshot
- nikSnapshot
- birthDateSnapshot
- npwpNumberSnapshot
- ktpPhotoSnapshot
- npwpPhotoSnapshot

**Tujuan:**

- Audit trail: data yang digunakan untuk pengajuan tidak berubah meskipun customer update profil
- Compliance: memudahkan tracking data historis

---

## Validasi Bisnis

### 1. Submit Loan Application

- ✅ User profile harus sudah lengkap
- ✅ Tidak boleh ada pinjaman dengan status: PENDING_REVIEW, WAITING_APPROVAL, WAITING_DISBURSEMENT
- ✅ Amount ≤ Remaining Amount
- ✅ Tenor harus dalam rentang (tenorMin - tenorMax) dari plafond
- ✅ Plafond ID harus sesuai dengan plafond aktif user

### 2. Review & Approve

- ✅ Marketing & Branch Manager hanya bisa proses pinjaman di **branch mereka**
- ✅ Backoffice bisa proses pinjaman dari **semua branch**
- ✅ Status harus sesuai (PENDING_REVIEW untuk Marketing, WAITING_APPROVAL untuk Branch Manager, dll)
- ✅ Comment wajib jika REJECT

### 3. Remaining Amount Management

- ✅ Remaining amount berkurang saat submit
- ✅ Remaining amount dikembalikan jika reject
- ✅ Remaining amount **TIDAK bertambah** saat pinjaman lunas (tidak ada fitur pelunasan)

### 4. Access Control

- ✅ Customer hanya bisa lihat aplikasi miliknya sendiri
- ✅ Marketing hanya bisa proses aplikasi di branch-nya
- ✅ Branch Manager hanya bisa proses aplikasi di branch-nya
- ✅ Backoffice bisa akses semua branch

---

## Entity Structure

### **LoanApplication**

```java
- id (PK)
- user_id (FK) → users
- branch_id (FK) → branches
- plafond_id (FK) → plafonds
- amount
- tenor
- status (ENUM)
- submitted_at

// Snapshot fields
- full_name_snapshot
- phone_number_snapshot
- user_address_snapshot
- nik_snapshot
- birth_date_snapshot
- npwp_number_snapshot
- ktp_photo_snapshot
- npwp_photo_snapshot

// Additional fields
- occupation
- company_name
- rekening_number

// Document fields
- saving_book_cover
- payslip_photo
- spouse_ktp_photo
- marriage_certificate_photo
- employee_id_photo
```

### **ApplicationHistory**

```java
- id (PK)
- loan_application_id (FK) → loan_applications
- action_by_user_id (FK) → users
- status
- comment
- action_by_role
- created_at
```

---

## API Endpoints

### 1. **POST /api/loan-applications**

**Role:** CUSTOMER  
**Description:** Submit loan application  
**Content-Type:** multipart/form-data

**Request Body:**

- plafondId (Long, required)
- amount (BigDecimal, required)
- tenor (Integer, required)
- occupation (String, required)
- companyName (String, optional)
- rekeningNumber (String, required)
- savingBookCover (File, required)
- payslipPhoto (File, required)
- spouseKtpPhoto (File, optional)
- marriageCertificatePhoto (File, optional)
- employeeIdPhoto (File, optional)

**Response:**

```json
{
  "statusCode": 201,
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
    ...
  }
}
```

---

### 2. **GET /api/loan-applications/my**

**Role:** CUSTOMER  
**Description:** Get customer's own loan applications

**Response:**

```json
{
  "statusCode": 200,
  "message": "Berhasil mengambil data pengajuan pinjaman",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "amount": 5000000,
      "tenor": 12,
      "status": "PENDING_REVIEW",
      "submittedAt": "2024-01-15T10:30:00",
      ...
    }
  ]
}
```

---

### 3. **GET /api/loan-applications/{id}**

**Role:** CUSTOMER, MARKETING, BRANCH_MANAGER, BACKOFFICE, SUPERADMIN  
**Description:** Get loan application detail by ID

**Response:**

```json
{
  "statusCode": 200,
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
    "status": "WAITING_APPROVAL",
    "submittedAt": "2024-01-15T10:30:00",
    "fullNameSnapshot": "John Doe",
    "phoneNumberSnapshot": "081234567890",
    "occupation": "Software Engineer",
    ...
  }
}
```

---

### 4. **GET /api/loan-applications/{id}/history**

**Role:** CUSTOMER, MARKETING, BRANCH_MANAGER, BACKOFFICE, SUPERADMIN  
**Description:** Get loan application history

**Response:**

```json
{
  "statusCode": 200,
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
  ]
}
```

---

### 5. **GET /api/loan-applications/pending-review**

**Role:** MARKETING  
**Description:** Get list of loan applications with status PENDING_REVIEW in marketing's branch

**Response:**

```json
{
  "statusCode": 200,
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
      "submittedAt": "2024-01-15T10:30:00",
      ...
    }
  ]
}
```

---

### 6. **PUT /api/loan-applications/{id}/review**

**Role:** MARKETING  
**Description:** Review loan application (PROCEED or REJECT)

**Request Body:**

```json
{
  "action": "PROCEED", // or "REJECT"
  "comment": "Dokumen lengkap dan sesuai" // wajib jika REJECT
}
```

**Response:**

```json
{
  "statusCode": 200,
  "message": "Review berhasil diproses",
  "data": {
    "id": 1,
    "status": "WAITING_APPROVAL",
    ...
  }
}
```

---

### 7. **GET /api/loan-applications/waiting-approval**

**Role:** BRANCH_MANAGER  
**Description:** Get list of loan applications with status WAITING_APPROVAL in branch manager's branch

**Response:**

```json
{
  "statusCode": 200,
  "message": "Berhasil mengambil daftar pengajuan waiting approval",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "amount": 5000000,
      "status": "WAITING_APPROVAL",
      ...
    }
  ]
}
```

---

### 8. **PUT /api/loan-applications/{id}/approve**

**Role:** BRANCH_MANAGER  
**Description:** Approve loan application (APPROVE or REJECT)

**Request Body:**

```json
{
  "action": "APPROVE", // or "REJECT"
  "comment": "Pinjaman disetujui" // wajib jika REJECT
}
```

**Response:**

```json
{
  "statusCode": 200,
  "message": "Approval berhasil diproses",
  "data": {
    "id": 1,
    "status": "WAITING_DISBURSEMENT",
    ...
  }
}
```

---

### 9. **GET /api/loan-applications/waiting-disbursement**

**Role:** BACKOFFICE, SUPERADMIN  
**Description:** Get list of loan applications with status WAITING_DISBURSEMENT from all branches

**Response:**

```json
{
  "statusCode": 200,
  "message": "Berhasil mengambil daftar pengajuan waiting disbursement",
  "data": [
    {
      "id": 1,
      "userId": 5,
      "branchId": 1,
      "amount": 5000000,
      "status": "WAITING_DISBURSEMENT",
      ...
    }
  ]
}
```

---

### 10. **PUT /api/loan-applications/{id}/disburse**

**Role:** BACKOFFICE, SUPERADMIN  
**Description:** Disburse loan application

**Response:**

```json
{
  "statusCode": 200,
  "message": "Pinjaman berhasil dicairkan",
  "data": {
    "id": 1,
    "status": "DISBURSED",
    ...
  }
}
```

---

## Error Handling

### Common Errors:

1. **User profile belum lengkap**

   ```json
   {
     "statusCode": 400,
     "message": "Anda belum melengkapi profil. Silakan lengkapi profil terlebih dahulu"
   }
   ```

2. **Ada pinjaman aktif**

   ```json
   {
     "statusCode": 400,
     "message": "Anda masih memiliki pengajuan pinjaman yang sedang diproses. Silakan tunggu hingga proses selesai sebelum mengajukan pinjaman baru"
   }
   ```

3. **Amount melebihi remaining amount**

   ```json
   {
     "statusCode": 400,
     "message": "Jumlah pinjaman (10000000) melebihi sisa plafond Anda (5000000)"
   }
   ```

4. **Tenor tidak valid**

   ```json
   {
     "statusCode": 400,
     "message": "Tenor harus antara 6 - 36 bulan untuk plafond Bronze"
   }
   ```

5. **Branch access denied**

   ```json
   {
     "statusCode": 404,
     "message": "Loan application tidak ditemukan atau bukan di branch Anda"
   }
   ```

6. **Status tidak sesuai**

   ```json
   {
     "statusCode": 400,
     "message": "Loan application tidak dalam status PENDING_REVIEW. Status saat ini: WAITING_APPROVAL"
   }
   ```

7. **Comment wajib saat reject**
   ```json
   {
     "statusCode": 400,
     "message": "Comment wajib diisi jika melakukan reject"
   }
   ```

---

## Testing Scenarios

### Scenario 1: Happy Path - Full Flow

1. Customer lengkapi profile → **Success**
2. Customer submit loan application → **PENDING_REVIEW**
3. Marketing review → PROCEED → **WAITING_APPROVAL**
4. Branch Manager approve → APPROVE → **WAITING_DISBURSEMENT**
5. Backoffice disburse → **DISBURSED**

### Scenario 2: Reject by Marketing

1. Customer submit loan application → **PENDING_REVIEW**
2. Marketing review → REJECT → **REJECTED**
3. Remaining amount dikembalikan

### Scenario 3: Reject by Branch Manager

1. Customer submit → **PENDING_REVIEW**
2. Marketing → PROCEED → **WAITING_APPROVAL**
3. Branch Manager → REJECT → **REJECTED**
4. Remaining amount dikembalikan

### Scenario 4: Multiple Active Loans

1. Customer submit loan 1 → **PENDING_REVIEW**
2. Customer submit loan 2 → **Error: Ada pinjaman aktif**

### Scenario 5: Branch Access Control

1. Customer di Branch A submit loan → **PENDING_REVIEW**
2. Marketing di Branch B coba review → **Error: Bukan di branch Anda**
3. Marketing di Branch A review → **Success**

---

## Database Schema

### loan_applications

```sql
CREATE TABLE loan_applications (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  branch_id BIGINT NOT NULL REFERENCES branches(id),
  plafond_id BIGINT NOT NULL REFERENCES plafonds(id),
  amount DECIMAL(19,2) NOT NULL,
  tenor INTEGER NOT NULL,
  status VARCHAR(50) NOT NULL,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- Snapshot fields
  full_name_snapshot VARCHAR(255),
  phone_number_snapshot VARCHAR(20),
  user_address_snapshot TEXT,
  nik_snapshot VARCHAR(16),
  birth_date_snapshot DATE,
  npwp_number_snapshot VARCHAR(20),
  ktp_photo_snapshot VARCHAR(255),
  npwp_photo_snapshot VARCHAR(255),

  -- Additional fields
  occupation VARCHAR(100) NOT NULL,
  company_name VARCHAR(255),
  rekening_number VARCHAR(50) NOT NULL,

  -- Document fields
  saving_book_cover VARCHAR(255) NOT NULL,
  payslip_photo VARCHAR(255) NOT NULL,
  spouse_ktp_photo VARCHAR(255),
  marriage_certificate_photo VARCHAR(255),
  employee_id_photo VARCHAR(255)
);
```

### application_histories

```sql
CREATE TABLE application_histories (
  id BIGSERIAL PRIMARY KEY,
  loan_application_id BIGINT NOT NULL REFERENCES loan_applications(id),
  action_by_user_id BIGINT NOT NULL REFERENCES users(id),
  status VARCHAR(50) NOT NULL,
  comment TEXT,
  action_by_role VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Notes & Best Practices

1. **Snapshot Data**: Selalu gunakan data snapshot saat approval/disbursement, bukan data real-time dari user profile
2. **Remaining Amount**: Tidak ada fitur pelunasan, jadi remaining amount hanya berkurang (tidak bertambah)
3. **Branch Control**: Marketing dan Branch Manager harus strict di branch mereka
4. **Comment on Reject**: Wajib memberikan alasan yang jelas saat reject
5. **File Upload**: Validasi tipe file dan ukuran sebelum upload
6. **Transaction**: Gunakan @Transactional untuk consistency data
7. **History Tracking**: Setiap perubahan status harus dicatat di history

---

**Created by:** Loanova Development Team  
**Last Updated:** 2024-01-15
