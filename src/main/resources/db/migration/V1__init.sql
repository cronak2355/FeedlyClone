create table account(
    id bigint auto_increment primary key
    ,email varchar(255) not null unique
    ,password varchar(512) not null
);
