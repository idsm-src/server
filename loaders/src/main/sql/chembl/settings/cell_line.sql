create index cell_line_bases__chembl_id on chembl.cell_line_bases(chembl_id);
create index cell_line_bases__label on chembl.cell_line_bases(label);
create index cell_line_bases__description on chembl.cell_line_bases(description);
create index cell_line_bases__organism on chembl.cell_line_bases(organism);
create index cell_line_bases__taxonomy on chembl.cell_line_bases(taxonomy);
create index cell_line_bases__cellosaurus on chembl.cell_line_bases(cellosaurus);
create index cell_line_bases__clo on chembl.cell_line_bases(clo_id);
create index cell_line_bases__efo on chembl.cell_line_bases(efo_unit, efo_id);
grant select on chembl.cell_line_bases to sparql;
