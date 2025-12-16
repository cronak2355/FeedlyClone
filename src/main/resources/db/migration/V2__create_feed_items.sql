create table feeds (
    id bigint auto_increment primary key,
    company_id bigint not null,
    url varchar(500) not null unique,
    title varchar(300),
    description varchar(500),
    created_at datetime not null
);

create table feed_items (
    id bigint auto_increment primary key,
    feed_id bigint not null,
    user_id bigint not null,
    title varchar(500) not null,
    summary varchar(2000) not null,
    image_url varchar(500),
    source varchar(200) not null,
    published_at datetime not null,
    is_read boolean not null,
    constraint fk_feed_item_feed
        foreign key (feed_id) references feeds(id)
);
