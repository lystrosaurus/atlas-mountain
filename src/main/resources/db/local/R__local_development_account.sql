-- LOCAL-ONLY: known development credentials must never be loaded in production.
INSERT INTO sys_user (
    id, username, password_hash, nickname, status,
    created_at, created_by, updated_at, updated_by, deleted
)
SELECT
    1,
    'admin',
    '$2a$10$4NPnJZ2elMZEpQ./DFD4v.cFWYeXvfboH72HFMjLDmJ03MpVQ13A6',
    'Local Admin',
    'ENABLED',
    NOW(),
    0,
    NOW(),
    0,
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_user
    WHERE id = 1 OR username = 'admin'
);
