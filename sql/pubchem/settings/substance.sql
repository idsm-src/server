create index substance_bases__source on pubchem.substance_bases(source);
create index substance_bases__available on pubchem.substance_bases(available);
create index substance_bases__modified on pubchem.substance_bases(modified);
create index substance_bases__compound on pubchem.substance_bases(compound);
grant select on pubchem.substance_bases to sparql;

--------------------------------------------------------------------------------

create index substance_types__substance on pubchem.substance_types(substance);
create index substance_types__chebi on pubchem.substance_types(chebi);
grant select on pubchem.substance_types to sparql;

--------------------------------------------------------------------------------

create index substance_chembl_matches__substance on pubchem.substance_chembl_matches(substance);
create index substance_chembl_matches__chembl on pubchem.substance_chembl_matches(chembl);
grant select on pubchem.substance_chembl_matches to sparql;

--------------------------------------------------------------------------------

create index substance_glytoucan_matches__glytoucan on pubchem.substance_glytoucan_matches(glytoucan);
grant select on pubchem.substance_glytoucan_matches to sparql;

--------------------------------------------------------------------------------

create index substance_references__substance on pubchem.substance_references(substance);
create index substance_references__reference on pubchem.substance_references(reference);
grant select on pubchem.substance_references to sparql;

--------------------------------------------------------------------------------

create index substance_pdblinks__substance on pubchem.substance_pdblinks(substance);
create index substance_pdblinks__pdblink on pubchem.substance_pdblinks(pdblink);
grant select on pubchem.substance_pdblinks to sparql;

--------------------------------------------------------------------------------

create index substance_synonyms__substance on pubchem.substance_synonyms(substance);
create index substance_synonyms__synonym on pubchem.substance_synonyms(synonym);
grant select on pubchem.substance_synonyms to sparql;
