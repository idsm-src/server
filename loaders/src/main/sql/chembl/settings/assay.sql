create index assays__chembl_id on chembl.assays(chembl_id);
create index assays__type on chembl.assays(type);
create index assays__description on chembl.assays using hash (description);
create index assays__document on chembl.assays(document);
create index assays__target on chembl.assays(target);
create index assays__source on chembl.assays(source);
create index assays__cell_line on chembl.assays(cell_line);
create index assays__format_id on chembl.assays(format_id);
create index assays__organism on chembl.assays(organism);
create index assays__taxonomy on chembl.assays(taxonomy);
create index assays__category on chembl.assays(category);
create index assays__cell_type on chembl.assays(cell_type);
create index assays__strain on chembl.assays(strain);
create index assays__tissue on chembl.assays(tissue);
create index assays__subcellular_fraction on chembl.assays(subcellular_fraction);
create index assays__test_type on chembl.assays(test_type);
create index assays__relationship_type on chembl.assays(relationship_type);
create index assays__relationship_desc on chembl.assays(relationship_desc);
create index assays__confidence_score on chembl.assays(confidence_score);
create index assays__confidence_desc on chembl.assays(confidence_desc);
create index assays__pubchem_assay on chembl.assays(pubchem_assay);
create index assays__pubchem_bioassay on chembl.assays(pubchem_bioassay);
grant select on chembl.assays to sparql;

--------------------------------------------------------------------------------

create index assay_reference_labels__reference on chembl.assay_reference_labels(reference);
create index assay_reference_labels__label on chembl.assay_reference_labels(label);
grant select on chembl.assay_reference_labels to sparql;
