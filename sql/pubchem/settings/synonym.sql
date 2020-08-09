grant select on pubchem.synonym_bases to sparql;

--------------------------------------------------------------------------------

create index synonym_values__synonym on pubchem.synonym_values(synonym);
create index synonym_values__value__gin on pubchem.synonym_values using gin (to_tsvector('english', value));
grant select on pubchem.synonym_values to sparql;

--------------------------------------------------------------------------------

create index synonym_types__synonym on pubchem.synonym_types(synonym);
create index synonym_types__type on pubchem.synonym_types(type_id);
grant select on pubchem.synonym_types to sparql;

--------------------------------------------------------------------------------

create index synonym_compounds__synonym on pubchem.synonym_compounds(synonym);
create index synonym_compounds__compound on pubchem.synonym_compounds(compound);
grant select on pubchem.synonym_compounds to sparql;

--------------------------------------------------------------------------------

create index synonym_mesh_subjects__synonym on pubchem.synonym_mesh_subjects(synonym);
create index synonym_mesh_subjects__subject on pubchem.synonym_mesh_subjects(subject);
grant select on pubchem.synonym_mesh_subjects to sparql;

--------------------------------------------------------------------------------

create index synonym_concept_subjects__synonym on pubchem.synonym_concept_subjects(synonym);
create index synonym_concept_subjects__concept on pubchem.synonym_concept_subjects(concept);
grant select on pubchem.synonym_concept_subjects to sparql;
