--liquibase formatted sql
--changeset jano:1
create table profile_view_records (
  id bigint not null,
  created timestamp,
  profile_id bigint not null,
  user_name varchar(50) not null,
  primary key (id)
);

create table profiles (
  id bigint not null,
  user_description varchar(255),
  user_id bigint not null,
  primary key (id)
);

create table users (
  id bigint not null,
  user_name varchar(50) not null,
  primary key (id)
);

create index user_name_index on users (user_name);
create index profile_record_index on profile_view_records (profile_id, created);

alter table users add constraint uq_user_name  unique (user_name);
alter table profiles add constraint fk_user_id foreign key (user_id) references users;

create sequence profile_view_rec_seq start with 1 increment by 50;
create sequence user_seq start with 1 increment by 50;

