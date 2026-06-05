DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM (
            SELECT LOWER(BTRIM(username)) AS normalized_username
            FROM app_user
            GROUP BY LOWER(BTRIM(username))
            HAVING COUNT(*) > 1
        ) duplicate_usernames
    ) THEN
        RAISE EXCEPTION 'Найдены конфликтующие логины, различающиеся только регистром или пробелами.';
    END IF;
END
$$;

UPDATE app_user
SET username = LOWER(BTRIM(username))
WHERE username <> LOWER(BTRIM(username));

ALTER TABLE app_user
DROP CONSTRAINT IF EXISTS chk_app_user_username_normalized;

ALTER TABLE app_user
ADD CONSTRAINT chk_app_user_username_normalized
CHECK (username = LOWER(BTRIM(username)));

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_username_lower
ON app_user (LOWER(username));
