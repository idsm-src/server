create index substance_bases__parent_id on molmedb.substance_bases(parent_id);
create index substance_bases__charge on molmedb.substance_bases(charge);
create index substance_bases__ph_start on molmedb.substance_bases(ph_start);
create index substance_bases__ph_end on molmedb.substance_bases(ph_end);
create index substance_bases__molecular_weight on molmedb.substance_bases(molecular_weight);
create index substance_bases__logp on molmedb.substance_bases(logp);
create index substance_bases__identifier on molmedb.substance_bases(identifier);
create index substance_bases__canonical_smiles on molmedb.substance_bases(canonical_smiles);
create index substance_bases__inchi on molmedb.substance_bases(inchi);
create index substance_bases__inchikey on molmedb.substance_bases(inchikey);
grant select on molmedb.substance_bases to sparql;

--------------------------------------------------------------------------------

create index substance_identifiers__substance_id on molmedb.substance_identifiers(substance_id);
create index substance_identifiers__type on molmedb.substance_identifiers(type);
create index substance_identifiers__value on molmedb.substance_identifiers(value);
grant select on molmedb.substance_identifiers to sparql;

--------------------------------------------------------------------------------

create index substance_links__substance_id on molmedb.substance_links(substance_id);
create index substance_links__type on molmedb.substance_links(type);
create index substance_links__value on molmedb.substance_links(value);
grant select on molmedb.substance_links to sparql;

--------------------------------------------------------------------------------

create index obsoleted_substances__value on molmedb.obsoleted_substances(substance_id);
grant select on molmedb.obsoleted_substances to sparql;
