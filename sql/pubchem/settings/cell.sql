create index cell_bases__organism on pubchem.cell_bases(organism);
create index cell_bases__label on pubchem.cell_bases(label);
grant select on pubchem.cell_bases to sparql;

--------------------------------------------------------------------------------

create index cell_alternatives__cell on pubchem.cell_alternatives(cell);
create index cell_alternatives__alternative on pubchem.cell_alternatives(alternative);
grant select on pubchem.cell_alternatives to sparql;

--------------------------------------------------------------------------------

create index cell_occurrences__cell on pubchem.cell_occurrences(cell);
create index cell_occurrences__occurrence on pubchem.cell_occurrences(occurrence);
grant select on pubchem.cell_occurrences to sparql;

--------------------------------------------------------------------------------

create index cell_references__cell on pubchem.cell_references(cell);
create index cell_references__reference on pubchem.cell_references(reference);
grant select on pubchem.cell_references to sparql;

--------------------------------------------------------------------------------

create index cell_matches__cell on pubchem.cell_matches(cell);
create index cell_matches__match on pubchem.cell_matches(match_unit, match_id);
grant select on pubchem.cell_matches to sparql;

--------------------------------------------------------------------------------

create index cell_mesh_matches__cell on pubchem.cell_mesh_matches(cell);
create index cell_mesh_matches__match on pubchem.cell_mesh_matches(match);
grant select on pubchem.cell_mesh_matches to sparql;

--------------------------------------------------------------------------------

create index cell_wikidata_matches__cell on pubchem.cell_wikidata_matches(cell);
create index cell_wikidata_matches__match on pubchem.cell_wikidata_matches(match);
grant select on pubchem.cell_wikidata_matches to sparql;

--------------------------------------------------------------------------------

create index cell_cancerrxgene_matches__cell on pubchem.cell_cancerrxgene_matches(cell);
create index cell_cancerrxgene_matches__match on pubchem.cell_cancerrxgene_matches(match);
grant select on pubchem.cell_cancerrxgene_matches to sparql;

--------------------------------------------------------------------------------

create index cell_depmap_matches__cell on pubchem.cell_depmap_matches(cell);
create index cell_depmap_matches__match on pubchem.cell_depmap_matches(match);
grant select on pubchem.cell_depmap_matches to sparql;

--------------------------------------------------------------------------------

create index cell_sanger_passport_matches__cell on pubchem.cell_sanger_passport_matches(cell);
create index cell_sanger_passport_matches__match on pubchem.cell_sanger_passport_matches(match);
grant select on pubchem.cell_sanger_passport_matches to sparql;

--------------------------------------------------------------------------------

create index cell_sanger_line_matches__cell on pubchem.cell_sanger_line_matches(cell);
create index cell_sanger_line_matches__match on pubchem.cell_sanger_line_matches(match);
grant select on pubchem.cell_sanger_line_matches to sparql;

--------------------------------------------------------------------------------

create index cell_cellosaurus_matches__cell on pubchem.cell_cellosaurus_matches(cell);
create index cell_cellosaurus_matches__match on pubchem.cell_cellosaurus_matches(match);
grant select on pubchem.cell_cellosaurus_matches to sparql;

--------------------------------------------------------------------------------

create index cell_chembl_card_matches__cell on pubchem.cell_chembl_card_matches(cell);
create index cell_chembl_card_matches__match on pubchem.cell_chembl_card_matches(match);
grant select on pubchem.cell_chembl_card_matches to sparql;

--------------------------------------------------------------------------------

create index cell_anatomies__cell on pubchem.cell_anatomies(cell);
create index cell_anatomies__anatomy on pubchem.cell_anatomies(anatomy);
grant select on pubchem.cell_anatomies to sparql;
