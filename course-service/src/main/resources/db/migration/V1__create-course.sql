create table if not exists course
(
    id          uuid primary key        default gen_random_uuid(),
    name        varchar unique not null,
    description text           not null,
    date        timestamp      not null default now()
);

create index idx_course_date on course (date);