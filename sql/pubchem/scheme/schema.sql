create table schema_foreign_keys
(
    __                 integer not null,
    parent_table       varchar not null,
    parent_columns     varchar[] not null,
    foreign_table      varchar not null,
    foreign_columns    varchar[] not null,
    primary key(__)
);


create table schema_unjoinable_columns
(
    __               integer not null,
    left_table       varchar not null,
    left_columns     varchar[] not null,
    right_table      varchar not null,
    right_columns    varchar[] not null,
    primary key(__)
);
