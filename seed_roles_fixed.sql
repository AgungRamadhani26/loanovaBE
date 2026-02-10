USE loanova_db;
GO

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted_at) 
VALUES ('CUSTOMER', 'Customer Role', GETDATE(), GETDATE(), NULL);

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted_at) 
VALUES ('STAFF', 'Staff Role', GETDATE(), GETDATE(), NULL);

INSERT INTO roles (role_name, role_description, created_at, updated_at, deleted_at) 
VALUES ('ADMIN', 'Admin Role', GETDATE(), GETDATE(), NULL);
GO
