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

create index taxonomy_matches__taxonomy on pubchem.taxonomy_matches(taxonomy);
create index taxonomy_matches__match on pubchem.taxonomy_matches(match_unit, match_id);
grant select on pubchem.taxonomy_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_mesh_matches__taxonomy on pubchem.taxonomy_mesh_matches(taxonomy);
create index taxonomy_mesh_matches__match on pubchem.taxonomy_mesh_matches(match);
grant select on pubchem.taxonomy_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index taxonomy_catalogueoflife_matches__taxonomy on pubchem.taxonomy_catalogueoflife_matches(taxonomy);
create index taxonomy_catalogueoflife_matches__match on pubchem.taxonomy_catalogueoflife_matches(match);
grant select on pubchem.taxonomy_catalogueoflife_matches to sparql;
