--liquibase formatted sql

--changeset burgasvv:1
create table if not exists file (
    id uuid primary key default gen_random_uuid() ,
    name varchar not null ,
    content_type varchar not null ,
    size bigint not null check ( size >= 0 ) ,
    data bytea not null
);

create index idx_file_name on file(name);