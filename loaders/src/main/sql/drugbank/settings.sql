create index compound_bases__molfile on drugbank.compound_bases using hash (molfile);
grant select on drugbank.compound_bases to sparql;
