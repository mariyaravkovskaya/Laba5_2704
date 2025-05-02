CREATE TABLE IF NOT EXISTS readers(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name varchar(50) not null,
    surname varchar(100) not null,
    subscribed boolean not null default false,
    phone varchar(15) not null
);

CREATE TABLE IF NOT EXISTS books(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name varchar(100) not null,
    isbn varchar(100) not null,
    publishing_year INTEGER not null,
    author varchar(100) not null,
    publisher varchar(100) not null
);
