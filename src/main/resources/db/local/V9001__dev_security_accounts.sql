-- =========================================================
-- DEV-УЧЕТНЫЕ ЗАПИСИ ДЛЯ ЭТАПА 3
-- =========================================================

UPDATE app_user
SET username = 'superadmin',
    password_hash = '$2a$10$vyitszaJufWN7Wn/wF2/cumsclahIQYtveMWqJgfGYLwAsi3qUMmG',
    last_login_at = NULL
WHERE username = 'superadmin';

UPDATE app_user
SET username = 'dispatcher',
    password_hash = '$2a$10$aVEwE42hUL2W6XRc7UAqh.UvfKB5YoKCdTXmczm8cz5ORmO3t.qUi',
    last_login_at = NULL
WHERE username = 'dispatcher1';

UPDATE app_user
SET username = 'mechanic',
    password_hash = '$2a$10$oxHwJ6.kgRSECFxe.ZnFfO6oZsbrAyXM78EuYWy6uzhAnE8vZQvgC',
    last_login_at = NULL
WHERE username = 'mechanic1';

UPDATE app_user
SET username = 'hr',
    password_hash = '$2a$10$.z.f7UcVQeL751Gryi.eL.zWeB.UCTUbt9mF0I0PT8slsLGNRZjjS',
    last_login_at = NULL
WHERE username = 'hr1';

UPDATE app_user
SET username = 'viewer',
    password_hash = '$2a$10$3jMEkwE9qD.x3G27yaesBu2inv3nK1Bcavwn54fqyqy5a/rU6mxB6',
    last_login_at = NULL
WHERE username = 'viewer1';

INSERT INTO app_user (username, password_hash, employee_id, is_active, created_at, last_login_at)
VALUES ('admin', '$2a$10$8wAWCuj8QFy/2ePCm8HOMu.WEQ9HoJvFitUIC1zUE/qrZejzCow/q', NULL, TRUE, CURRENT_TIMESTAMP, NULL);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
CROSS JOIN role r
WHERE u.username = 'admin'
  AND r.name = 'ADMIN';
