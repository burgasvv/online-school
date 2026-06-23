create table if not exists course_identity
(
    course_id   uuid references course (id) on delete cascade on update cascade,
    identity_id uuid not null,
    primary key (course_id, identity_id)
);

create index idx_course_identity on course_identity(course_id, identity_id);