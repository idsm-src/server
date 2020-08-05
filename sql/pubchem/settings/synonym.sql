grant select on synonym_bases to sparql;

--------------------------------------------------------------------------------

create index synonym_values__synonym on synonym_values(synonym);
create index synonym_values__value__gin on synonym_values using gin (to_tsvector('english', value));
grant select on synonym_values to sparql;

--------------------------------------------------------------------------------

create index synonym_types__synonym on synonym_types(synonym);
create index synonym_types__type on synonym_types(type_id);
grant select on synonym_types to sparql;

--------------------------------------------------------------------------------

create index synonym_compounds__synonym on synonym_compounds(synonym);
create index synonym_compounds__compound on synonym_compounds(compound);
grant select on synonym_compounds to sparql;

--------------------------------------------------------------------------------

create index synonym_mesh_subjects__synonym on synonym_mesh_subjects(synonym);
create index synonym_mesh_subjects__subject on synonym_mesh_subjects(subject);
grant select on synonym_mesh_subjects to sparql;

--------------------------------------------------------------------------------

create index synonym_concept_subjects__synonym on synonym_concept_subjects(synonym);
create index synonym_concept_subjects__concept on synonym_concept_subjects(concept);
grant select on synonym_concept_subjects to sparql;
