-- ============================================================================
-- MIGRASI: Menambahkan kolom interest_rate_snapshot pada tabel loan_applications
-- Database: SQL Server
-- Tanggal: 2026-02-01
-- Deskripsi: Kolom ini menyimpan snapshot interest rate dari plafond saat
--            customer mengajukan pinjaman. Tujuannya agar rate yang disepakati
--            tidak berubah meskipun rate di master plafond diupdate.
-- ============================================================================

-- 1. Tambah kolom baru (NULLABLE dulu untuk data existing)
ALTER TABLE loan_applications 
ADD interest_rate_snapshot DECIMAL(5,2);
GO

-- 2. Update data existing: ambil interest_rate dari plafond terkait
UPDATE la
SET la.interest_rate_snapshot = p.interest_rate
FROM loan_applications la
INNER JOIN plafonds p ON p.id = la.plafond_id
WHERE la.interest_rate_snapshot IS NULL;
GO

-- 3. Ubah kolom menjadi NOT NULL setelah data terisi
ALTER TABLE loan_applications 
ALTER COLUMN interest_rate_snapshot DECIMAL(5,2) NOT NULL;
GO

-- ============================================================================
-- VERIFIKASI
-- ============================================================================
-- Jalankan query ini untuk memastikan migrasi berhasil:
-- SELECT id, amount, tenor, interest_rate_snapshot, plafond_id FROM loan_applications;
