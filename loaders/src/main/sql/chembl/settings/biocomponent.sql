create index biocomponents__chembl_id on chembl.biocomponents(chembl_id);
create index biocomponents__type on chembl.biocomponents(type);
create index biocomponents__description on chembl.biocomponents(description);
create index biocomponents__organism on chembl.biocomponents(organism);
create index biocomponents__taxonomy on chembl.biocomponents(taxonomy);
create index biocomponents__sequence on chembl.biocomponents using hash (sequence);
grant select on chembl.biocomponents to sparql;
