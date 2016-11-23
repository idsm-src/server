sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:synonym
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:synonym .;

--------------------------------------------------------------------------------

drop function iri_synonym;
drop function iri_synonym_INVERSE;

--------------------------------------------------------------------------------

drop table synonym_concept_subjects;
drop table synonym_mesh_subjects;
drop table synonym_compounds;
drop table synonym_types;
drop table synonym_values;
drop table synonym_bases;
