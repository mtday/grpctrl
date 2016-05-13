
--
-- Note that this script is intended to be run against PostgreSQL and contains some PostgreSQL-specific SQL statements.
--
-- CREATE DATABASE grpctrl;
-- CREATE USER grpctrl WITH PASSWORD 'password';
-- ALTER ROLE grpctrl WITH CREATEDB;
--


CREATE TABLE accounts (
    account_id       BIGSERIAL     NOT NULL,

    name             VARCHAR(200)  NOT NULL,

    CONSTRAINT accounts_pk PRIMARY KEY (account_id)
);

ALTER SEQUENCE accounts_account_id_seq RESTART WITH 10000;


CREATE TABLE api_logins (
    account_id       BIGINT        NOT NULL,

    key              VARCHAR(20)   NOT NULL,
    secret           VARCHAR(40)   NOT NULL,

    CONSTRAINT api_logins_uniq UNIQUE (key, secret),
    CONSTRAINT api_logins_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE service_levels (
    account_id       BIGINT        NOT NULL,

    max_groups       INTEGER       NOT NULL,
    max_tags         INTEGER       NOT NULL,
    max_depth        INTEGER       NOT NULL,

    CONSTRAINT service_levels_pk PRIMARY KEY (account_id),
    CONSTRAINT service_levels_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE users (
    user_id          BIGSERIAL     NOT NULL,

    login            VARCHAR(200)  NOT NULL,
    source           VARCHAR(20)   NOT NULL,
    created          DATETIME      NOT NULL,
    is_active        BOOLEAN       NOT NULL,

    CONSTRAINT users_pk PRIMARY KEY (user_id),
    CONSTRAINT users_uniq_login UNIQUE (login, source)
);

ALTER SEQUENCE users_user_id_seq RESTART WITH 10000;

-- The initial admin account.
INSERT INTO users (user_id, login, source) VALUES (1, 'admin', 'LOCAL');


CREATE TABLE user_emails (
    user_id          BIGINT        NOT NULL,

    email            VARCHAR(200)  NOT NULL,
    is_primary       BOOLEAN       NOT NULL,
    is_verified      BOOLEAN       NOT NULL,

    CONSTRAINT user_emails_pk PRIMARY KEY (user_id),
    CONSTRAINT user_emails_uniq_email UNIQUE (email),
    CONSTRAINT user_emails_fk_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

INSERT INTO user_emails (user_id, email, is_primary, is_verified) VALUES (1, 'admin@grpctrl.com', true, true);


CREATE TABLE user_auths (
    user_id          BIGINT        NOT NULL,

    hash_alg         VARCHAR(20)   NOT NULL,
    salt             VARCHAR(20)   NOT NULL,
    hashed_pass      VARCHAR(128)  NOT NULL, -- the size of SHA-512 is 128

    CONSTRAINT user_auths_pk PRIMARY KEY (user_id),
    CONSTRAINT user_auths_fk_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

INSERT INTO user_auths (user_id, hash_alg, salt, hashed_pass) VALUES (1, 'SHA-256', '46Hr5XhP',
    'c5a9e98748609144ce345d98424dbc3009502f57a9243686fe8eca17ff716ef4');


CREATE TABLE user_roles (
    user_id          BIGINT        NOT NULL,

    role             VARCHAR(20)   NOT NULL,

    CONSTRAINT user_roles_pk PRIMARY KEY (user_id, role),
    CONSTRAINT user_roles_fk_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');


CREATE TABLE user_accounts (
    user_id          BIGINT        NOT NULL,
    account_id       BIGINT        NOT NULL,

    CONSTRAINT user_accounts_pk PRIMARY KEY (user_id, account_id),
    CONSTRAINT user_accounts_fk_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT user_accounts_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE groups (
    group_id         BIGSERIAL     NOT NULL,

    account_id       BIGINT        NOT NULL,
    parent_id        BIGINT,
    group_name       VARCHAR(200)  NOT NULL,

    CONSTRAINT groups_pk PRIMARY KEY (group_id),
    CONSTRAINT groups_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE,

    -- These two constraints guarantee a child group and a parent group are in the same account.
    CONSTRAINT groups_uniq_parent UNIQUE (account_id, group_id),
    CONSTRAINT groups_fk_parent FOREIGN KEY (account_id, parent_id) REFERENCES groups (account_id, group_id) ON DELETE CASCADE
);

CREATE INDEX groups_idx_group_name ON groups (group_name);

-- These name partial indexes prevent duplicate names of child groups within the same parent.
CREATE UNIQUE INDEX groups_uniq_name_with_parent ON groups (account_id, parent_id, group_name) WHERE parent_id IS NOT NULL;
CREATE UNIQUE INDEX groups_uniq_name_without_parent ON groups (account_id, group_name) WHERE parent_id IS NULL;

ALTER SEQUENCE groups_group_id_seq RESTART WITH 10000;


CREATE TABLE tags (
    account_id       BIGINT        NOT NULL,
    group_id         BIGINT        NOT NULL,

    tag_label        VARCHAR(200)  NOT NULL,
    tag_value        VARCHAR(200)  NOT NULL,

    CONSTRAINT tags_pk PRIMARY KEY (group_id, tag_label, tag_value),
    CONSTRAINT tags_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE,
    CONSTRAINT tags_fk_groups FOREIGN KEY (group_id) REFERENCES groups (group_id) ON DELETE CASCADE
);

CREATE INDEX tags_idx_tag_label ON tags (tag_label);

