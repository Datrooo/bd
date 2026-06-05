DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'auto_enterprise_app') THEN
        CREATE ROLE auto_enterprise_app
            LOGIN
            PASSWORD 'auto_enterprise_app'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'auto_enterprise_sql_console') THEN
        CREATE ROLE auto_enterprise_sql_console
            LOGIN
            PASSWORD 'auto_enterprise_sql_console'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT;
    END IF;
END
$$;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE auto_enterprise TO auto_enterprise_app;
GRANT CONNECT ON DATABASE auto_enterprise TO auto_enterprise_sql_console;

GRANT USAGE ON SCHEMA public TO auto_enterprise_app;
GRANT USAGE ON SCHEMA public TO auto_enterprise_sql_console;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO auto_enterprise_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO auto_enterprise_app;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO auto_enterprise_sql_console;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO auto_enterprise_sql_console;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO auto_enterprise_app;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO auto_enterprise_app;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA public
GRANT SELECT ON TABLES TO auto_enterprise_sql_console;

ALTER DEFAULT PRIVILEGES FOR USER postgres IN SCHEMA public
GRANT SELECT ON SEQUENCES TO auto_enterprise_sql_console;
