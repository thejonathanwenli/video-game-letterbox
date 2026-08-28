drop database if exists video_game_letterbox;
create database video_game_letterbox;
use video_game_letterbox;

create table app_user (
    user_id bigint primary key auto_increment,
    username varchar(50) not null,
    email varchar(254) not null,
    password_hash varchar(2048) not null,
    enabled bit not null default 1,
    bio varchar(500),
    created_at datetime(6) not null,
    constraint uq_app_user_username unique (username),
    constraint uq_app_user_email unique (email)
);

create table app_role (
    role_id bigint primary key auto_increment,
    `name` varchar(50) not null,
    constraint uq_app_role_name unique (`name`)
);

create table app_user_role (
    user_id bigint not null,
    role_id bigint not null,
    constraint pk_app_user_role primary key (user_id, role_id),
    constraint fk_app_user_role_user
        foreign key (user_id) references app_user(user_id)
        on delete cascade,
    constraint fk_app_user_role_role
        foreign key (role_id) references app_role(role_id)
        on delete cascade
);

create table game (
    game_id bigint primary key auto_increment,
    rawg_id int not null,
    title varchar(255) not null,
    `description` text,
    image_url varchar(1000),
    release_date date,
    imported_at datetime(6) not null,
    rawg_rating decimal(4,2),
    constraint uq_game_rawg_id unique (rawg_id)
);

create table genre (
    genre_id bigint primary key auto_increment,
    rawg_id int not null,
    `name` varchar(100) not null,
    constraint uq_genre_rawg_id unique (rawg_id),
    constraint uq_genre_name unique (`name`)
);

create table game_genre (
    game_id bigint not null,
    genre_id bigint not null,
    constraint pk_game_genre primary key (game_id, genre_id),
    constraint fk_game_genre_game
        foreign key (game_id) references game(game_id)
        on delete cascade,
    constraint fk_game_genre_genre
        foreign key (genre_id) references genre(genre_id)
        on delete cascade
);

create table user_game (
    user_game_id bigint primary key auto_increment,
    user_id bigint not null,
    game_id bigint not null,
    `status` varchar(30) not null,
    personal_rating tinyint,
    recommended boolean,
    added_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uq_user_game_user_game unique (user_id, game_id),
    constraint ck_user_game_status
        check (`status` in ('WANT_TO_PLAY', 'PLAYING', 'COMPLETED', 'DROPPED')),
    constraint ck_user_game_personal_rating
        check (personal_rating is null or personal_rating between 1 and 10),
    constraint fk_user_game_user
        foreign key (user_id) references app_user(user_id)
        on delete cascade,
    constraint fk_user_game_game
        foreign key (game_id) references game(game_id)
);

create table review (
    review_id bigint primary key auto_increment,
    user_game_id bigint not null,
    body text not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uq_review_user_game unique (user_game_id),
    constraint fk_review_user_game
        foreign key (user_game_id) references user_game(user_game_id)
        on delete cascade
);

create table user_follow (
    follower_id bigint not null,
    followed_id bigint not null,
    created_at datetime(6) not null,
    constraint pk_user_follow primary key (follower_id, followed_id),
    constraint ck_user_follow_not_self check (follower_id <> followed_id),
    constraint fk_user_follow_follower
        foreign key (follower_id) references app_user(user_id)
        on delete cascade,
    constraint fk_user_follow_followed
        foreign key (followed_id) references app_user(user_id)
        on delete cascade
);

insert into app_role (`name`) values ('USER');

