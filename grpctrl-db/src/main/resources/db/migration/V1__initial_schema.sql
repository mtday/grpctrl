
-- CREATE DATABASE grpctrl;
-- CREATE USER grpctrl WITH PASSWORD 'password';
-- ALTER ROLE grpctrl WITH CREATEDB;


CREATE TABLE accounts (
    account_id       VARCHAR(30)   NOT NULL,

    CONSTRAINT accounts_pk PRIMARY KEY (account_id)
);


CREATE TABLE service_levels (
    account_id       VARCHAR(30)   NOT NULL,
    max_groups       INTEGER       NOT NULL,
    max_tags         INTEGER       NOT NULL,
    max_children     INTEGER       NOT NULL,
    max_depth        INTEGER       NOT NULL,

    CONSTRAINT service_levels_fk_accounts FOREIGN KEY (account_id)
        REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE groups (
    account_id       VARCHAR(30)   NOT NULL,
    parent_id        VARCHAR(200)  NOT NULL,
    group_id         VARCHAR(200)  NOT NULL,

    CONSTRAINT groups_pk PRIMARY KEY (account_id, group_id, parent_id),
    CONSTRAINT groups_fk_accounts FOREIGN KEY (account_id)
        REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE tags (
    account_id       VARCHAR(30)   NOT NULL,
    parent_id        VARCHAR(200)  NOT NULL,
    group_id         VARCHAR(200)  NOT NULL,
    tag_label        VARCHAR(200)  NOT NULL,
    tag_value        VARCHAR(200)  NOT NULL,

    CONSTRAINT tags_pk PRIMARY KEY (account_id, group_id, parent_id, tag_label, tag_value),
    CONSTRAINT tags_fk_groups FOREIGN KEY (account_id, group_id, parent_id)
        REFERENCES groups (account_id, group_id, parent_id) ON DELETE CASCADE
);

