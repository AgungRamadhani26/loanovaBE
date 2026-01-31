-- Migration Script: Add Location Fields to LoanApplication
-- Date: 2026-01-31
-- Description: Menambahkan kolom latitude dan longitude untuk menyimpan lokasi pengajuan

ALTER TABLE loan_applications ADD latitude FLOAT NOT NULL DEFAULT 0;
ALTER TABLE loan_applications ADD longitude FLOAT NOT NULL DEFAULT 0;

-- Setelah migrasi berhasil, hapus default value (untuk data baru wajib isi)
-- Jika menggunakan SQL Server:
-- ALTER TABLE loan_applications ALTER COLUMN latitude FLOAT NOT NULL;
-- ALTER TABLE loan_applications ALTER COLUMN longitude FLOAT NOT NULL;
