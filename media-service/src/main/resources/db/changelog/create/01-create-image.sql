--liquibase formatted sql

--changeset burgasvv:1
create table if not exists image
(
    id           uuid primary key default gen_random_uuid(),
    name         varchar not null,
    content_type varchar not null,
    preview      boolean not null default true,
    size         bigint  not null check ( size >= 0 ),
    data         bytea   not null
);

create index idx_image_name on image (name);
create index idx_image_preview on image (preview);