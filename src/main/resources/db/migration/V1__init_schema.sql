CREATE TABLE sys_user (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE api_token (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    token_prefix VARCHAR(32) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by BIGINT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_api_token_prefix UNIQUE (token_prefix),
    CONSTRAINT uk_api_token_hash UNIQUE (token_hash)
);

INSERT INTO sys_user (
    id, username, password_hash, nickname, status,
    created_at, created_by, updated_at, updated_by, deleted
) VALUES (
    1,
    'admin',
    -- NOSONAR: LOCAL-ONLY dev seed password (admin/atlas-local)
    '$2a$10$4NPnJZ2elMZEpQ./DFD4v.cFWYeXvfboH72HFMjLDmJ03MpVQ13A6',
    'Local Admin',
    'ENABLED',
    NOW(),
    0,
    NOW(),
    0,
    0
);
