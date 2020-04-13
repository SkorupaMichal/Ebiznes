-- !Ups

create table category(
    id integer not null primary key autoincrement,
    name varchar(255) not null
);

create table product(
    id integer not null primary key autoincrement,
    name varchar(255) not null,
    category_id integer not null,
    FOREIGN  key (category_id) references  category(id)
);

-- !Downs

drop table category;
drop table product;