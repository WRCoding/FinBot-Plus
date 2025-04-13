create table record
(
    record_no  TEXT not null
        primary key,
    date       TEXT not null,
    amount     TEXT not null,
    type       TEXT not null,
    remark     TEXT,
    sub_remark TEXT,
    created_at TIMESTAMP default CURRENT_TIMESTAMP,
    updated_at TIMESTAMP default CURRENT_TIMESTAMP
);

