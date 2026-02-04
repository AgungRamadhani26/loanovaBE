-- ============================================================================
-- SQL Script: Make Password Column Nullable
-- Purpose: Allow NULL password for Google Sign-In users
-- Date: 2026-02-04
-- ============================================================================

-- Untuk SQL Server (yang digunakan di loanova)
ALTER TABLE users
ALTER COLUMN password VARCHAR(255) NULL;

-- ============================================================================
-- CATATAN:
-- 
-- 1. Password nullable HANYA untuk user dengan auth_provider = 'GOOGLE'
-- 2. User dengan auth_provider = 'LOCAL' WAJIB punya password
-- 3. Validasi password dilakukan di level aplikasi:
--    - Login form: wajib isi password
--    - Google Sign-In: tidak perlu password
--
-- QUERY untuk verifikasi:
-- SELECT username, email, auth_provider, 
--        CASE WHEN password IS NULL THEN 'NULL' ELSE 'HAS_PASSWORD' END as password_status
-- FROM users;
-- ============================================================================
