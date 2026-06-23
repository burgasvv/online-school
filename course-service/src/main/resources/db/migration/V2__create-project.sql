create table if not exists project
(
    id          uuid primary key default gen_random_uuid(),
    name        varchar unique not null,
    description text           not null,
    link        text unique    not null,
    task_id     uuid unique,
    date        timestamp        default now()
);