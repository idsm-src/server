create index biocomponent_bases__chembl_id on chembl.biocomponent_bases(chembl_id);
create index biocomponent_bases__type on chembl.biocomponent_bases(type);
create index biocomponent_bases__description on chembl.biocomponent_bases(description);
create index biocomponent_bases__organism on chembl.biocomponent_bases(organism);
create index biocomponent_bases__taxonomy on chembl.biocomponent_bases(taxonomy);
create index biocomponent_bases__sequence on chembl.biocomponent_bases using hash (sequence);
grant select on chembl.biocomponent_bases to sparql;
