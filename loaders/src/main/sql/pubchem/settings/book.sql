create index book_bases__title on pubchem.book_bases(title);
create index book_bases__publisher on pubchem.book_bases(publisher);
create index book_bases__location on pubchem.book_bases(location);
create index book_bases__subtitle on pubchem.book_bases(subtitle);
create index book_bases__date on pubchem.book_bases(date);
create index book_bases__isbn on pubchem.book_bases(isbn);
grant select on pubchem.book_bases to sparql;

--------------------------------------------------------------------------------

create index book_authors__book on pubchem.book_authors(book);
create index book_authors__author on pubchem.book_authors(author);
grant select on pubchem.book_authors to sparql;
