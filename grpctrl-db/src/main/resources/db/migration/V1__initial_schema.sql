
CREATE TABLE accounts (
    account_id       VARCHAR(30)   NOT NULL,

    PRIMARY KEY (account_id)
);


CREATE TABLE service_levels (
    account_id       VARCHAR(30)   NOT NULL,
    max_groups       INTEGER       NOT NULL,
    max_tags         INTEGER       NOT NULL,
    max_children     INTEGER       NOT NULL,
    max_depth        INTEGER       NOT NULL,

    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);


CREATE TABLE groups (
    account_id       VARCHAR(30)   NOT NULL,
    group_id         VARCHAR(200)  NOT NULL,
    parent_id        VARCHAR(200),

    PRIMARY KEY (account_id, group_id),
    FOREIGN KEY (account_id, parent_id) REFERENCES groups (account_id, group_id) ON DELETE CASCADE
);


CREATE TABLE tags (
    account_id       VARCHAR(30)   NOT NULL,
    group_id         VARCHAR(200)  NOT NULL,
    tag_label        VARCHAR(200)  NOT NULL,
    tag_value        VARCHAR(200)  NOT NULL,

    PRIMARY KEY (account_id, group_id, tag_label, tag_value),
    FOREIGN KEY (account_id, group_id) REFERENCES groups (account_id, group_id) ON DELETE CASCADE
);

