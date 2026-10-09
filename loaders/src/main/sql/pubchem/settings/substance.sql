create index substances__id__varchar on pubchem.substances((id::varchar));
create index substances__source on pubchem.substances(source);
create index substances__available on pubchem.substances(available);
create index substances__modified on pubchem.substances(modified);
create index substances__compound on pubchem.substances(compound);
grant select on pubchem.substances to sparql;

--------------------------------------------------------------------------------

create index substance_types__substance on pubchem.substance_types(substance);
create index substance_types__chebi on pubchem.substance_types(chebi);
grant select on pubchem.substance_types to sparql;

--------------------------------------------------------------------------------

create index substance_chembl_matches__substance on pubchem.substance_chembl_matches(substance);
create index substance_chembl_matches__match on pubchem.substance_chembl_matches(match);
grant select on pubchem.substance_chembl_matches to sparql;

--------------------------------------------------------------------------------

create index substance_glytoucan_matches__match on pubchem.substance_glytoucan_matches(match);
grant select on pubchem.substance_glytoucan_matches to sparql;

--------------------------------------------------------------------------------

create index substance_references__substance on pubchem.substance_references(substance);
create index substance_references__reference on pubchem.substance_references(reference);
grant select on pubchem.substance_references to sparql;

--------------------------------------------------------------------------------

create index substance_patents__substance on pubchem.substance_patents(substance);
create index substance_patents__patent on pubchem.substance_patents(patent);
grant select on pubchem.substance_patents to sparql;

--------------------------------------------------------------------------------

create index substance_pdblinks__substance on pubchem.substance_pdblinks(substance);
create index substance_pdblinks__pdblink on pubchem.substance_pdblinks(pdblink);
grant select on pubchem.substance_pdblinks to sparql;

--------------------------------------------------------------------------------

create index substance_synonyms__substance on pubchem.substance_synonyms(substance);
create index substance_synonyms__synonym on pubchem.substance_synonyms(synonym);
grant select on pubchem.substance_synonyms to sparql;
