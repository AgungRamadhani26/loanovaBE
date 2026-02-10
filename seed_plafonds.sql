USE loanova_db;
GO

INSERT INTO plafonds (name, description, max_amount, interest_rate, tenor_min, tenor_max, created_at, updated_at, deleted_at) 
VALUES ('BRONZE', 'Bronze Plafond Package', 5000000.00, 2.50, 3, 12, GETDATE(), GETDATE(), NULL);

INSERT INTO plafonds (name, description, max_amount, interest_rate, tenor_min, tenor_max, created_at, updated_at, deleted_at) 
VALUES ('SILVER', 'Silver Plafond Package', 10000000.00, 2.00, 6, 24, GETDATE(), GETDATE(), NULL);

INSERT INTO plafonds (name, description, max_amount, interest_rate, tenor_min, tenor_max, created_at, updated_at, deleted_at) 
VALUES ('GOLD', 'Gold Plafond Package', 20000000.00, 1.50, 12, 36, GETDATE(), GETDATE(), NULL);
GO
