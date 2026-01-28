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
| `POST` | `/refresh` | Refresh access token menggunakan refresh token. | Public |
| `POST` | `/logout` | Logout user dan invalidsi token. | Authenticated |
| `POST` | `/forgot-password` | Request link reset password ke email. | Public |
| `POST` | `/reset-password` | Reset password menggunakan token dari email. | Public |
| `POST` | `/change-password` | Mengganti password saat user sedang login. | Authenticated |

---

## 2. User Management
### Users
**Base Path**: `/api/users`

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Mengambil daftar semua user. | `USER:READ` (Superadmin) |
| `POST` | `/` | Membuat user internal baru. | `USER:CREATE` (Superadmin) |
| `GET` | `/{id}` | Mengambil detail user berdasarkan ID. | `USER:DETAILS` (Superadmin) |
| `PUT` | `/{id}` | Memperbarui data user. | `USER:UPDATE` (Superadmin) |
| `DELETE` | `/{id}` | Menghapus user. | `USER:DELETE` (Superadmin) |

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
*Generated by Loanova Assistant*
