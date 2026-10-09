create index cell_lines__chembl_id on chembl.cell_lines(chembl_id);
create index cell_lines__label on chembl.cell_lines(label);
create index cell_lines__description on chembl.cell_lines(description);
create index cell_lines__organism on chembl.cell_lines(organism);
create index cell_lines__taxonomy on chembl.cell_lines(taxonomy);
create index cell_lines__cellosaurus on chembl.cell_lines(cellosaurus);
create index cell_lines__clo_id on chembl.cell_lines(clo_id);
create index cell_lines__efo_unit_efo_id on chembl.cell_lines(efo_unit, efo_id);
grant select on chembl.cell_lines to sparql;
