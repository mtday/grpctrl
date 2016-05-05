
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


CREATE TABLE service_levels (
    account_id       BIGINT        NOT NULL,

    max_groups       INTEGER       NOT NULL,
    max_tags         INTEGER       NOT NULL,
    max_depth        INTEGER       NOT NULL,

    CONSTRAINT service_levels_pk PRIMARY KEY (account_id),
    CONSTRAINT service_levels_fk_accounts FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
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

