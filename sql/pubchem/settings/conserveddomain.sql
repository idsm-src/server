create index conserveddomain_bases__title on pubchem.conserveddomain_bases(title);
create index conserveddomain_bases__abstract on pubchem.conserveddomain_bases using hash(abstract);
grant select on pubchem.conserveddomain_bases to sparql;
