create index molecule_references__molecule_id on chembl_tmp.molecule_references(molecule_id);
create index molecule_references__reference_type on chembl_tmp.molecule_references(reference_type);
create index molecule_references__reference on chembl_tmp.molecule_references(reference);
grant select on chembl_tmp.molecule_references to sparql;

--------------------------------------------------------------------------------

create index molecule_pubchem_references__molecule_id on chembl_tmp.molecule_pubchem_references(molecule_id);
create index molecule_pubchem_references__compound_id on chembl_tmp.molecule_pubchem_references(compound_id);
grant select on chembl_tmp.molecule_pubchem_references to sparql;

--------------------------------------------------------------------------------

create index molecule_chebi_references__molecule_id on chembl_tmp.molecule_chebi_references(molecule_id);
create index molecule_chebi_references__chebi_id on chembl_tmp.molecule_chebi_references(chebi_id);
grant select on chembl_tmp.molecule_chebi_references to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.molecule_reference_types as
    select distinct reference_type, reference from chembl_tmp.molecule_references;

grant select on chembl_tmp.molecule_reference_types to sparql;
