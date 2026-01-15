# Documentation: Role-Based Access Control (RBAC) Permissions

This document provides a comprehensive list of all granular permissions available in the Loanova system. These permissions are used in `@PreAuthorize("hasAuthority('...')")` annotations within controllers and are seeded via `PermissionSeeder.java`.

## Permission Groupings

### 1. Authentication & Security
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `AUTH:LOGOUT` | Terminate current session and invalidate token. | ALL |
| `AUTH:CHANGE_PASSWORD` | Change the password of the currently logged-in user. | ALL |

### 2. Loan Management
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `LOAN:SUBMIT` | Submit a new loan application. | CUSTOMER, SUPERADMIN* |
| `LOAN:READ_MY` | View loan applications submitted by the current user. | CUSTOMER, SUPERADMIN* |
| `LOAN:DETAILS` | View detailed information of any specific loan application. | ALL |
| `LOAN:HISTORY` | View the status transition history (audit trail) of a loan. | ALL |
| `LOAN:LIST_PENDING_REVIEW` | List loans waiting for initial review (Marketing bucket). | MARKETING |
| `LOAN:REVIEW` | Perform review (Accept/Reject) on a loan application. | MARKETING |
| `LOAN:LIST_WAITING_APPROVAL`| List loans waiting for Branch Manager approval. | BRANCHMANAGER |
| `LOAN:APPROVE` | Approve or Reject a loan application. | BRANCHMANAGER |
| `LOAN:LIST_WAITING_DISBURSE`| List approved loans waiting for fund disbursement. | BACKOFFICE |
| `LOAN:DISBURSE` | Mark a loan as disbursed. | BACKOFFICE |

### 3. Branch Management
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `BRANCH:READ` | View list of all active branches. | ALL |
| `BRANCH:CREATE` | Add a new office branch to the system. | SUPERADMIN |
| `BRANCH:UPDATE` | Edit existing branch details (Name, Address, etc.). | SUPERADMIN |
| `BRANCH:DELETE` | Soft-delete a branch. | SUPERADMIN |
| `BRANCH:RESTORE` | Restore a previously deleted branch. | SUPERADMIN |

### 4. User & Role Management
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `USER:READ` | List all registered users (staff and customers). | SUPERADMIN |
| `USER:DETAILS` | View detailed profile and role of a specific user. | SUPERADMIN |
| `USER:CREATE` | Register a new user manually (Admin only). | SUPERADMIN |
| `USER:UPDATE` | Modify user account status or basic information. | SUPERADMIN |
| `USER:DELETE` | Remove a user from the system. | SUPERADMIN |
| `ROLE:READ` | List available roles. | SUPERADMIN |
| `ROLE:CREATE` | Create a new system role. | SUPERADMIN |
| `ROLE:UPDATE` | Update role descriptions. | SUPERADMIN |
| `ROLE:DELETE` | Delete a role. | SUPERADMIN |

### 5. Plafond Management
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `PLAFOND:READ` | List all loan plafond configurations. | SUPERADMIN |
| `PLAFOND:DETAILS` | View details of a specific plafond. | SUPERADMIN |
| `PLAFOND:CREATE` | Create new plafond limits. | SUPERADMIN |
| `PLAFOND:UPDATE` | Edit plafond amount or criteria. | SUPERADMIN |
| `PLAFOND:DELETE` | Delete a plafond configuration. | SUPERADMIN |
| `PLAFOND:RESTORE` | Recover a deleted plafond. | SUPERADMIN |

### 6. User Plafond (Credit Scoring)
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `USER_PLAFOND:ASSIGN` | Link a specific plafond limit to a user account. | SUPERADMIN, BACKOFFICE |
| `USER_PLAFOND:READ` | View which plafond is currently active for a user. | SUPERADMIN, BACKOFFICE |

### 7. Profile Management
| Permission | Description | Assigned Roles |
| :--- | :--- | :--- |
| `PROFILE:COMPLETE` | Fill in mandatory KYB/KYC details for the first time. | CUSTOMER |
| `PROFILE:UPDATE` | Update own profile information. | CUSTOMER |
| `PROFILE:READ_MY` | View own profile data. | CUSTOMER |

---

## Role Assignment Summary Matrix

| Role | Access Scope |
| :--- | :--- |
| **SUPERADMIN** | Full administrative control over Users, Roles, Branches, and Plafonds. |
| **MARKETING** | Discovery of branches and full processing of the Initial Review stage. |
| **BRANCHMANAGER** | Discovery of branches and full processing of the Approval stage. |
| **BACKOFFICE** | Full processing of Disbursement stage and Plafond Assignment. |
| **CUSTOMER** | Submission of loans, tracking own status, and profile management. |

> [!NOTE]
> *SUPERADMIN permissions for LOAN:SUBMIT and LOAN:READ_MY were intentionally omitted per strict controller comments analysis to ensure separation of duties.*
