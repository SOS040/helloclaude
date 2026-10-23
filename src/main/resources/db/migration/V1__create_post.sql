create table post (
    id         bigint generated always as identity primary key,
    title      varchar(200) not null,
    content    text         not null,
    author     varchar(50)  not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null
);

create index idx_post_created_at on post (created_at);
