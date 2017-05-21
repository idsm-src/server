create function substance(id in integer);
create function substance_inverse(iri in varchar);
create function substance_chembl(id in integer);
create function substance_chembl_inverse(iri in varchar);
create function substance_ebi_chembl(id in integer);
create function substance_ebi_chembl_inverse(iri in varchar);

--------------------------------------------------------------------------------

drop table substance_synonyms;
drop table substance_pdblinks;
drop table substance_references;
drop table substance_matches;
drop table substance_types;
drop table substance_bases;
