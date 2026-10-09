create index substances__parent on molmedb.substances(parent);
create index substances__charge on molmedb.substances(charge);
create index substances__ph_start on molmedb.substances(ph_start);
create index substances__ph_end on molmedb.substances(ph_end);
create index substances__molecular_weight on molmedb.substances(molecular_weight);
create index substances__logp on molmedb.substances(logp);
create index substances__identifier on molmedb.substances(identifier);
create index substances__canonical_smiles on molmedb.substances(canonical_smiles);
create index substances__inchi on molmedb.substances(inchi);
create index substances__inchikey on molmedb.substances(inchikey);
grant select on molmedb.substances to sparql;

--------------------------------------------------------------------------------

create index substance_identifiers__substance on molmedb.substance_identifiers(substance);
create index substance_identifiers__type on molmedb.substance_identifiers(type);
create index substance_identifiers__value on molmedb.substance_identifiers(value);
grant select on molmedb.substance_identifiers to sparql;

--------------------------------------------------------------------------------

create index substance_links__substance on molmedb.substance_links(substance);
create index substance_links__type on molmedb.substance_links(type);
create index substance_links__value on molmedb.substance_links(value);
grant select on molmedb.substance_links to sparql;

--------------------------------------------------------------------------------

create index substance_obsolete_identifiers__substance on molmedb.substance_obsolete_identifiers(substance);
grant select on molmedb.substance_obsolete_identifiers to sparql;
