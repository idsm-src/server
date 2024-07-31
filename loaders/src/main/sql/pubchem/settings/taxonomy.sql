create index taxonomy_bases__label on pubchem.taxonomy_bases(label);
grant select on pubchem.taxonomy_bases to sparql;

--------------------------------------------------------------------------------

create index taxonomy_alternatives__taxonomy on pubchem.taxonomy_alternatives(taxonomy);
create index taxonomy_alternatives__alternative on pubchem.taxonomy_alternatives(alternative);
grant select on pubchem.taxonomy_alternatives to sparql;

--------------------------------------------------------------------------------

create index taxonomy_references__taxonomy on pubchem.taxonomy_references(taxonomy);
create index taxonomy_references__reference on pubchem.taxonomy_references(reference);
grant select on pubchem.taxonomy_references to sparql;

--------------------------------------------------------------------------------

grant select on pubchem.taxonomy_uniprot_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_mesh_matches__taxonomy on pubchem.taxonomy_mesh_matches(taxonomy);
create index taxonomy_mesh_matches__match on pubchem.taxonomy_mesh_matches(match);
grant select on pubchem.taxonomy_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_catalogueoflife_matches__taxonomy on pubchem.taxonomy_catalogueoflife_matches(taxonomy);
create index taxonomy_catalogueoflife_matches__match on pubchem.taxonomy_catalogueoflife_matches(match);
grant select on pubchem.taxonomy_catalogueoflife_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_thesaurus_matches__taxonomy on pubchem.taxonomy_thesaurus_matches(taxonomy);
create index taxonomy_thesaurus_matches__match on pubchem.taxonomy_thesaurus_matches(match);
grant select on pubchem.taxonomy_thesaurus_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_itis_matches__taxonomy on pubchem.taxonomy_itis_matches(taxonomy);
create index taxonomy_itis_matches__match on pubchem.taxonomy_itis_matches(match);
grant select on pubchem.taxonomy_itis_matches to sparql;
