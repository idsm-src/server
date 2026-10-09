create table pubchem.books
(
    id          integer not null,
    title       varchar,
    publisher   varchar,
    location    varchar,
    subtitle    varchar,
    date        varchar,
    isbn        varchar,
    primary key(id)
);


create table pubchem.book_authors
(
    book        integer not null,
    author      integer not null,
    primary key(book, author)
);
