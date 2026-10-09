create index compounds__molfile on drugbank.compounds using hash (molfile);
grant select on drugbank.compounds to sparql;
