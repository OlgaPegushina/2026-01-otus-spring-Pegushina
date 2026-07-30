create table if not exists authors (
    id bigserial,
    full_name varchar(255),
    primary key (id)
);

create table if not exists genres (
    id bigserial,
    name varchar(255),
    primary key (id)
);

create table if not exists books (
    id bigserial,
    title varchar(255),
    author_id bigint references authors (id) on delete cascade,
    primary key (id)
);

create table if not exists books_genres (
    book_id bigint references books(id) on delete cascade,
    genre_id bigint references genres(id) on delete cascade,
    primary key (book_id, genre_id)
);

create table if not exists comments (
    id bigserial,
    text varchar(255),
    book_id bigint references books (id) on delete cascade,
    created_by varchar(100) not null,
    primary key (id)
);

create table if not exists users (
    id bigserial,
    username varchar(100) unique not null,
    password varchar(100) not null,
    enabled boolean not null default true,
    primary key (id)
);

create table if not exists roles (
    id bigserial,
    name varchar(50) unique not null,
    primary key (id),
    constraint check_role_name check (name in ('ROLE_USER', 'ROLE_ADMIN'))
);

create table if not exists user_roles (
    user_id bigint not null references users(id) on delete cascade,
    role_id bigint not null references roles(id) on delete cascade,
    primary key (user_id, role_id)
);