--liquibase formatted sql

--changeset burgasvv:1
create table if not exists document
(
    id           uuid primary key default gen_random_uuid(),
    name         varchar not null,
    content_type varchar not null,
    size         bigint  not null check ( size >= 0 ),
    data         bytea   not null
);

create index idx_document_name on document (name);