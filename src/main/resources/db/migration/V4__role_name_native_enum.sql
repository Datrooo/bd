DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'app_role'
    ) THEN
        CREATE TYPE app_role AS ENUM (
            'SUPERADMIN',
            'ADMIN',
            'DISPATCHER',
            'HR',
            'MECHANIC',
            'VIEWER'
        );
    END IF;
END
$$;

ALTER TABLE role
DROP CONSTRAINT IF EXISTS chk_role_name_enum;

ALTER TABLE role
ALTER COLUMN name TYPE app_role
USING name::app_role;
