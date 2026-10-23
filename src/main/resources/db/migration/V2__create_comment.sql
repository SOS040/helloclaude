create table comment (
    id         bigint generated always as identity primary key,
    post_id    bigint        not null,
    content    varchar(1000) not null,
    author     varchar(50)   not null,
    created_at timestamp(6)  not null,
    updated_at timestamp(6)  not null,
    -- 게시글 삭제 시 댓글도 함께 삭제한다
    constraint fk_comment_post foreign key (post_id) references post (id) on delete cascade
);

create index idx_comment_post_id on comment (post_id);
