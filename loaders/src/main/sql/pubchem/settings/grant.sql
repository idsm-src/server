create index grants__number on pubchem.grants(number);
create index grants__organization on pubchem.grants(organization);
grant select on pubchem.grants to sparql;
