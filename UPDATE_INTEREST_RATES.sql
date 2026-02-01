-- ============================================================================
-- UPDATE INTEREST RATES - Menyesuaikan bunga plafond agar lebih realistis
-- Database: SQL Server
-- Tanggal: 2026-02-01
-- 
-- Konsep: Semakin tinggi tier, semakin KECIL bunganya (reward untuk customer loyal)
-- ============================================================================

-- Cek data sebelum update
SELECT id, name, interest_rate AS old_interest_rate, max_amount 
FROM plafonds 
ORDER BY max_amount ASC;
GO

-- ============================================================================
-- UPDATE INTEREST RATES
-- ============================================================================

-- Bronze: 1.50% per bulan (entry level, bunga tertinggi)
UPDATE plafonds 
SET interest_rate = 1.50 
WHERE LOWER(name) LIKE '%bronze%';
GO

-- Silver: 1.25% per bulan
UPDATE plafonds 
SET interest_rate = 1.25 
WHERE LOWER(name) LIKE '%silver%';
GO

-- Gold: 1.00% per bulan
UPDATE plafonds 
SET interest_rate = 1.00 
WHERE LOWER(name) LIKE '%gold%';
GO

-- Platinum: 0.75% per bulan (VIP, bunga terendah)
UPDATE plafonds 
SET interest_rate = 0.75 
WHERE LOWER(name) LIKE '%platinum%';
GO

-- ============================================================================
-- UPDATE LOAN APPLICATIONS SNAPSHOT (untuk data existing)
-- Sesuaikan interest_rate_snapshot dengan rate baru dari plafond
-- ============================================================================

UPDATE la
SET la.interest_rate_snapshot = p.interest_rate
FROM loan_applications la
INNER JOIN plafonds p ON p.id = la.plafond_id;
GO

-- ============================================================================
-- VERIFIKASI
-- ============================================================================

-- Cek plafonds setelah update
SELECT id, name, interest_rate, max_amount 
FROM plafonds 
ORDER BY interest_rate DESC;
GO

-- Cek loan applications dengan snapshot baru
SELECT 
    la.id,
    la.amount,
    la.tenor,
    la.interest_rate_snapshot,
    p.name AS plafond_name,
    p.interest_rate AS current_plafond_rate
FROM loan_applications la
INNER JOIN plafonds p ON p.id = la.plafond_id
ORDER BY la.id DESC;
GO

-- ============================================================================
-- SIMULASI PERHITUNGAN (untuk verifikasi)
-- Contoh: Pinjaman 10 juta, tenor 12 bulan, bunga 1% per bulan
-- Total Bunga = 10.000.000 x 1% x 12 = 1.200.000
-- Total Pelunasan = 10.000.000 + 1.200.000 = 11.200.000
-- Cicilan/bulan = 11.200.000 / 12 = 933.333
-- ============================================================================
