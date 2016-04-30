
CREATE TABLE accounts (
    id               VARCHAR(30)   NOT NULL,

    PRIMARY KEY (id)
);


CREATE TABLE objects (
    account_id       VARCHAR(30)   NOT NULL,
    object_id        VARCHAR(200)  NOT NULL,
    object_type      SMALLINT      NOT NULL,
    parent_id        VARCHAR(200),

    UNIQUE KEY (account_id, object_id, object_type, parent_id),

    FOREIGN KEY (parent_id) REFERENCES objects (object_id) ON DELETE CASCADE
);


CREATE TABLE tags (
    account_id       VARCHAR(30)   NOT NULL,
    object_id        VARCHAR(200)  NOT NULL,
    object_type      SMALLINT      NOT NULL,
    tag_label        VARCHAR(200)  NOT NULL,
    tag_value        VARCHAR(200)  NOT NULL,

    UNIQUE (account_id, object_id, object_type, tag_label, tag_value),

    FOREIGN KEY (account_id, object_id, object_type)
        REFERENCES objects (account_id, object_id, object_type) ON DELETE CASCADE
);