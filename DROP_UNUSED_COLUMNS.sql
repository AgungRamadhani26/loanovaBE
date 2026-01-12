-- ============================================
-- DROP UNUSED COLUMNS FROM LOAN_APPLICATIONS
-- ============================================
-- Menghapus 3 kolom yang sudah tidak digunakan:
-- 1. spouse_ktp_photo
-- 2. marriage_certificate_photo  
-- 3. employee_id_photo
--
-- Kolom ini sudah dihapus dari entity dan DTO
-- untuk mengurangi jumlah field dari 11 menjadi 8
-- agar tidak exceed Tomcat multipart limit (10)
-- ============================================

USE loanova_db;
GO

-- Cek apakah kolom masih ada
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_NAME = 'loan_applications'
    AND COLUMN_NAME IN ('spouse_ktp_photo', 'marriage_certificate_photo', 'employee_id_photo');
GO

-- Drop kolom spouse_ktp_photo jika ada
IF EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'loan_applications' 
    AND COLUMN_NAME = 'spouse_ktp_photo'
)
BEGIN
    ALTER TABLE loan_applications DROP COLUMN spouse_ktp_photo;
    PRINT 'Column spouse_ktp_photo dropped successfully';
END
ELSE
BEGIN
    PRINT 'Column spouse_ktp_photo does not exist';
END
GO

-- Drop kolom marriage_certificate_photo jika ada
IF EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'loan_applications' 
    AND COLUMN_NAME = 'marriage_certificate_photo'
)
BEGIN
    ALTER TABLE loan_applications DROP COLUMN marriage_certificate_photo;
    PRINT 'Column marriage_certificate_photo dropped successfully';
END
ELSE
BEGIN
    PRINT 'Column marriage_certificate_photo does not exist';
END
GO

-- Drop kolom employee_id_photo jika ada
IF EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'loan_applications' 
    AND COLUMN_NAME = 'employee_id_photo'
)
BEGIN
    ALTER TABLE loan_applications DROP COLUMN employee_id_photo;
    PRINT 'Column employee_id_photo dropped successfully';
END
ELSE
BEGIN
    PRINT 'Column employee_id_photo does not exist';
END
GO

-- Verifikasi: Tampilkan semua kolom yang tersisa
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_NAME = 'loan_applications'
ORDER BY 
    ORDINAL_POSITION;
GO

PRINT '====================================';
PRINT 'Cleanup completed successfully!';
PRINT '====================================';
GO
