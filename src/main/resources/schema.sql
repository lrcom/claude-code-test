-- users 表结构（H2 兼容，MyBatis-Plus 不自动建表，需手动初始化）
CREATE TABLE IF NOT EXISTS users
(
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    username   VARCHAR(32)  NOT NULL UNIQUE,
    email      VARCHAR(128) NOT NULL UNIQUE,
    password   VARCHAR(128) NOT NULL,
    status     VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
