create index conserveddomain_bases__title on pubchem.conserveddomain_bases(title);
create index conserveddomain_bases__abstract on pubchem.conserveddomain_bases using hash(abstract);
grant select on pubchem.conserveddomain_bases to sparql;

--------------------------------------------------------------------------------

create index conserveddomain_references__domain on pubchem.conserveddomain_references(domain);
create index conserveddomain_references__reference on pubchem.conserveddomain_references(reference);
grant select on pubchem.conserveddomain_references to sparql;
