create index books__title on pubchem.books(title);
create index books__publisher on pubchem.books(publisher);
create index books__location on pubchem.books(location);
create index books__subtitle on pubchem.books(subtitle);
create index books__date on pubchem.books(date);
create index books__isbn on pubchem.books(isbn);
grant select on pubchem.books to sparql;

--------------------------------------------------------------------------------

create index book_authors__book on pubchem.book_authors(book);
create index book_authors__author on pubchem.book_authors(author);
grant select on pubchem.book_authors to sparql;
