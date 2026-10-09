create index conserveddomains__title on pubchem.conserveddomains(title);
create index conserveddomains__abstract on pubchem.conserveddomains using hash(abstract);
grant select on pubchem.conserveddomains to sparql;

--------------------------------------------------------------------------------

create index conserveddomain_references__conserveddomain on pubchem.conserveddomain_references(conserveddomain);
create index conserveddomain_references__reference on pubchem.conserveddomain_references(reference);
grant select on pubchem.conserveddomain_references to sparql;
