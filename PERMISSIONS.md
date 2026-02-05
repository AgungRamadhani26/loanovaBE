# Documentation: Role-Based Access Control (RBAC) Permissions

Dokumentasi lengkap semua permission granular dalam sistem Loanova. Permission ini digunakan dalam anotasi `@PreAuthorize("hasAuthority('...')")` di controller dan di-seed melalui `PermissionSeeder.java`.

**Last Updated:** 2026-02-05

---

## Permission Reference Table

### 1. Authentication & Security
**Base Path:** `/api/auth`

| Permission             | Description                              | Controller Method       | Assigned Roles |
| :--------------------- | :--------------------------------------- | :---------------------- | :------------- |
| `AUTH:LOGOUT`          | Terminasi sesi dan invalidasi token.     | `AuthController.logout` | ALL            |
| `AUTH:CHANGE_PASSWORD` | Mengubah password saat sedang login.     | `AuthController.changePassword` | ALL   |

---

### 2. User Management
**Base Path:** `/api/users`

| Permission     | Description                               | Controller Method            | Assigned Roles |
| :------------- | :---------------------------------------- | :--------------------------- | :------------- |
| `USER:READ`    | Melihat daftar semua user.                | `UserController.getAllUsers` | SUPERADMIN     |
| `USER:DETAILS` | Melihat detail user berdasarkan ID.       | `UserController.getUserById` | SUPERADMIN     |
| `USER:CREATE`  | Membuat user internal baru.               | `UserController.createUser`  | SUPERADMIN     |
| `USER:UPDATE`  | Memperbarui data user.                    | `UserController.updateUser`  | SUPERADMIN     |
| `USER:DELETE`  | Menghapus user dari sistem.               | `UserController.deleteUser`  | SUPERADMIN     |

---

### 3. Role Management
**Base Path:** `/api/roles`

| Permission    | Description                      | Controller Method            | Assigned Roles |
| :------------ | :------------------------------- | :--------------------------- | :------------- |
| `ROLE:READ`   | Melihat daftar semua role.       | `RoleController.getAllRoles` | SUPERADMIN     |
| `ROLE:CREATE` | Membuat role custom baru.        | `RoleController.createRole`  | SUPERADMIN     |
| `ROLE:UPDATE` | Update role & permissions.       | `RoleController.updateRole`  | SUPERADMIN     |
| `ROLE:DELETE` | Menghapus role dari sistem.      | `RoleController.deleteRole`  | SUPERADMIN     |

---

### 4. Permission Management
**Base Path:** `/api/permissions`

| Permission        | Description                               | Controller Method                    | Assigned Roles |
| :---------------- | :---------------------------------------- | :----------------------------------- | :------------- |
| `PERMISSION:READ` | Melihat daftar semua hak akses di sistem. | `PermissionController.getAllPermissions` | SUPERADMIN |

---

### 5. Branch Management
**Base Path:** `/api/branches`

| Permission       | Description                           | Controller Method                | Assigned Roles       |
| :--------------- | :------------------------------------ | :------------------------------- | :------------------- |
| `BRANCH:READ`    | Melihat daftar semua cabang.          | `BranchController.getAllBranches`| ALL                  |
| `BRANCH:CREATE`  | Menambah cabang baru.                 | `BranchController.createBranch`  | SUPERADMIN           |
| `BRANCH:UPDATE`  | Mengubah data cabang.                 | `BranchController.updateBranch`  | SUPERADMIN           |
| `BRANCH:DELETE`  | Menghapus cabang (soft delete).       | `BranchController.deleteBranch`  | SUPERADMIN           |
| `BRANCH:RESTORE` | Memulihkan cabang yang sudah dihapus. | `BranchController.restoreBranch` | SUPERADMIN           |

---

### 6. Plafond Management
**Base Path:** `/api/plafonds`

| Permission        | Description                    | Controller Method                   | Assigned Roles |
| :---------------- | :----------------------------- | :---------------------------------- | :------------- |
| `PLAFOND:READ`    | Melihat daftar semua plafond.  | `PlafondController.getAllPlafonds`  | SUPERADMIN     |
| `PLAFOND:DETAILS` | Melihat detail plafond.        | `PlafondController.getPlafondById`  | SUPERADMIN     |
| `PLAFOND:CREATE`  | Membuat plafond baru.          | `PlafondController.createPlafond`   | SUPERADMIN     |
| `PLAFOND:DELETE`  | Menghapus plafond (soft del).  | `PlafondController.deletePlafond`   | SUPERADMIN     |

---

### 7. User Plafond Assignment
**Base Path:** `/api/user-plafonds`

| Permission            | Description                                     | Controller Method                           | Assigned Roles         |
| :-------------------- | :---------------------------------------------- | :------------------------------------------ | :--------------------- |
| `USER_PLAFOND:ASSIGN` | Assign plafond ke user (otomatis nonaktif lama) | `UserPlafondController.assignPlafond`       | SUPERADMIN, BACKOFFICE |
| `USER_PLAFOND:READ`   | Melihat plafond aktif milik user.               | `UserPlafondController.getActiveUserPlafond`| SUPERADMIN, BACKOFFICE, CUSTOMER (self) |
| `USER_PLAFOND:HISTORY`| Melihat riwayat semua plafond user.             | `UserPlafondController.getUserPlafondHistory`| SUPERADMIN, BACKOFFICE |

---

### 8. Loan Application Management
**Base Path:** `/api/loan-applications`

| Permission                   | Description                                   | Controller Method                              | Assigned Roles    |
| :--------------------------- | :-------------------------------------------- | :--------------------------------------------- | :---------------- |
| `LOAN:SUBMIT`                | Submit pengajuan pinjaman baru.               | `LoanApplicationController.submitLoanApplication` | CUSTOMER       |
| `LOAN:READ_MY`               | Melihat pengajuan pinjaman sendiri.           | `LoanApplicationController.getMyApplications`  | CUSTOMER          |
| `LOAN:READ_ALL`              | Melihat semua pengajuan (filter by role).     | `LoanApplicationController.getAllApplications` | ALL               |
| `LOAN:DETAILS`               | Melihat detail pengajuan pinjaman.            | `LoanApplicationController.getApplicationDetail` | ALL             |
| `LOAN:HISTORY`               | Melihat history status pengajuan.             | `LoanApplicationController.getApplicationHistory` | ALL            |
| `LOAN:LIST_PENDING_REVIEW`   | Melihat daftar pending review (Marketing).    | `LoanApplicationController.getPendingApplications` | MARKETING     |
| `LOAN:REVIEW`                | Review pengajuan (PROCEED/REJECT).            | `LoanApplicationController.reviewApplication`  | MARKETING         |
| `LOAN:LIST_WAITING_APPROVAL` | Melihat daftar waiting approval (BM).         | `LoanApplicationController.getWaitingApprovalApplications` | BRANCHMANAGER |
| `LOAN:APPROVE`               | Approve/Reject pengajuan pinjaman.            | `LoanApplicationController.approveApplication` | BRANCHMANAGER     |
| `LOAN:LIST_WAITING_DISBURSE` | Melihat daftar waiting disbursement.          | `LoanApplicationController.getWaitingDisbursementApplications` | BACKOFFICE |
| `LOAN:DISBURSE`              | Mencairkan dana pinjaman.                     | `LoanApplicationController.disburseApplication` | BACKOFFICE       |
| `LOAN:REJECT_BACKOFFICE`     | Menolak pencairan di tahap backoffice.        | `LoanApplicationController.backofficeReject`   | BACKOFFICE        |

---

### 9. Profile Management
**Base Path:** `/api/user-profiles`

| Permission         | Description                              | Controller Method                       | Assigned Roles |
| :----------------- | :--------------------------------------- | :-------------------------------------- | :------------- |
| `PROFILE:COMPLETE` | Melengkapi data profil (multipart form). | `UserProfileController.completeProfile` | CUSTOMER       |
| `PROFILE:UPDATE`   | Memperbarui data profil (multipart form).| `UserProfileController.updateProfile`   | CUSTOMER       |
| `PROFILE:READ_MY`  | Melihat profil sendiri.                  | `UserProfileController.getMyProfile`    | CUSTOMER       |

---

### 10. Menu Visibility (Sidebar)
**Purpose:** Kontrol visibility menu di sidebar, terpisah dari data access permissions.

| Permission           | Description                              | Assigned Roles                              |
| :------------------- | :--------------------------------------- | :------------------------------------------ |
| `MENU:DASHBOARD`     | Melihat menu Dashboard di sidebar.       | SUPERADMIN, MARKETING, BRANCHMANAGER, BACKOFFICE |
| `MENU:USERS`         | Melihat menu Users di sidebar.           | SUPERADMIN                                  |
| `MENU:BRANCH`        | Melihat menu Branch di sidebar.          | SUPERADMIN                                  |
| `MENU:ROLES`         | Melihat menu Role Permission di sidebar. | SUPERADMIN                                  |
| `MENU:LOAN`          | Melihat menu Loan Application di sidebar.| SUPERADMIN, MARKETING, BRANCHMANAGER, BACKOFFICE |
| `MENU:HISTORY`       | Melihat menu Application History.        | SUPERADMIN, MARKETING, BRANCHMANAGER, BACKOFFICE |
| `MENU:PLAFOND`       | Melihat menu Plafond di sidebar.         | SUPERADMIN                                  |
| `MENU:USER_PLAFOND`  | Melihat menu User Plafond di sidebar.    | SUPERADMIN, BACKOFFICE                      |

> [!IMPORTANT]
> **MENU:* vs *:READ**: `MENU:*` mengontrol visibility sidebar, sedangkan `*:READ` mengontrol akses data API. Contoh: MARKETING punya `BRANCH:READ` untuk dropdown, tapi tidak punya `MENU:BRANCH` sehingga menu Branch tidak muncul.

---

## Role Summary Matrix

| Role              | Total Permissions | Access Scope                                                              |
| :---------------- | :---------------: | :------------------------------------------------------------------------ |
| **SUPERADMIN**    | 34                | Full administrative control + All menu visibility.                       |
| **MARKETING**     | 11                | Branch data + Loan review stage + Dashboard/Loan/History menus.          |
| **BRANCHMANAGER** | 11                | Branch data + Loan approval stage + Dashboard/Loan/History menus.        |
| **BACKOFFICE**    | 15                | Loan disbursement + User Plafond + Dashboard/Loan/History/UserPlafond menus. |
| **CUSTOMER**      | 11                | Self-service: Profile, Loan submission & tracking, Plafond viewing.      |

---

## Role Permission Details

### SUPERADMIN
```
AUTH:LOGOUT, AUTH:CHANGE_PASSWORD
USER:READ, USER:DETAILS, USER:CREATE, USER:UPDATE, USER:DELETE
ROLE:READ, ROLE:CREATE, ROLE:UPDATE, ROLE:DELETE
PERMISSION:READ
BRANCH:READ, BRANCH:CREATE, BRANCH:UPDATE, BRANCH:DELETE, BRANCH:RESTORE
PLAFOND:READ, PLAFOND:DETAILS, PLAFOND:CREATE, PLAFOND:DELETE
USER_PLAFOND:ASSIGN, USER_PLAFOND:READ, USER_PLAFOND:HISTORY
LOAN:READ_ALL, LOAN:DETAILS, LOAN:HISTORY
MENU:DASHBOARD, MENU:USERS, MENU:BRANCH, MENU:ROLES
MENU:LOAN, MENU:HISTORY, MENU:PLAFOND, MENU:USER_PLAFOND
```

### MARKETING
```
AUTH:LOGOUT, AUTH:CHANGE_PASSWORD
BRANCH:READ
LOAN:READ_ALL, LOAN:DETAILS, LOAN:HISTORY, LOAN:LIST_PENDING_REVIEW, LOAN:REVIEW
MENU:DASHBOARD, MENU:LOAN, MENU:HISTORY
```

### BRANCHMANAGER
```
AUTH:LOGOUT, AUTH:CHANGE_PASSWORD
BRANCH:READ
LOAN:READ_ALL, LOAN:DETAILS, LOAN:HISTORY, LOAN:LIST_WAITING_APPROVAL, LOAN:APPROVE
MENU:DASHBOARD, MENU:LOAN, MENU:HISTORY
```

### BACKOFFICE
```
AUTH:LOGOUT, AUTH:CHANGE_PASSWORD
LOAN:READ_ALL, LOAN:DETAILS, LOAN:HISTORY
LOAN:LIST_WAITING_DISBURSE, LOAN:DISBURSE, LOAN:REJECT_BACKOFFICE
USER_PLAFOND:ASSIGN, USER_PLAFOND:READ, USER_PLAFOND:HISTORY
MENU:DASHBOARD, MENU:LOAN, MENU:HISTORY, MENU:USER_PLAFOND
```

### CUSTOMER
```
AUTH:LOGOUT, AUTH:CHANGE_PASSWORD
BRANCH:READ
LOAN:SUBMIT, LOAN:READ_MY, LOAN:READ_ALL, LOAN:DETAILS, LOAN:HISTORY
PROFILE:COMPLETE, PROFILE:UPDATE, PROFILE:READ_MY
USER_PLAFOND:READ
```

---

## Public Endpoints (No Permission Required)

Beberapa endpoint dapat diakses tanpa autentikasi:

| Endpoint                     | Description                           |
| :--------------------------- | :------------------------------------ |
| `POST /api/auth/register`    | Mendaftarkan customer baru.           |
| `POST /api/auth/login`       | Login user (semua role).              |
| `POST /api/auth/firebase-google` | Login via Google Firebase.        |
| `POST /api/auth/refresh`     | Refresh access token.                 |
| `POST /api/auth/forgot-password` | Request link reset password.      |
| `POST /api/auth/reset-password` | Reset password dengan token.       |
| `GET /api/plafonds/public`   | Melihat daftar plafond untuk umum.    |

---

## Authenticated Endpoints (No Specific Permission)

Beberapa endpoint hanya memerlukan autentikasi tanpa permission spesifik:

| Endpoint                        | Description                              |
| :------------------------------ | :--------------------------------------- |
| `GET /api/notifications`        | Melihat notifikasi user yang login.      |
| `PUT /api/notifications/{id}/read` | Tandai notifikasi sebagai sudah dibaca. |
| `PUT /api/notifications/read-all` | Tandai semua notifikasi sudah dibaca.  |
| `GET /api/users/by-username/{username}` | Melihat profil sendiri (SpEL check). |

---

## Special Access Notes

> [!NOTE]
> **hasRole vs hasAuthority**: Endpoint `POST /api/notifications/test-push` menggunakan `hasRole('SUPERADMIN')` bukan `hasAuthority()`. Ini adalah akses berbasis role langsung, bukan permission granular.

> [!TIP]
> **USER_PLAFOND:READ untuk CUSTOMER**: Customer dapat melihat plafond aktif milik sendiri melalui pengecekan SpEL di controller (`isSelf || isAdmin`).

---

*Generated by Loanova Assistant - Last Updated: 2026-02-05*
