USE loanova_db;
GO

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted) 
VALUES ('CUSTOMER', 'Customer Role', GETDATE(), GETDATE(), 0);

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted) 
VALUES ('STAFF', 'Staff Role', GETDATE(), GETDATE(), 0);

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted) 
VALUES ('ADMIN', 'Admin Role', GETDATE(), GETDATE(), 0);
GO
