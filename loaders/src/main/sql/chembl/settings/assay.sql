create index assay_bases__chembl_id on chembl.assay_bases(chembl_id);
create index assay_bases__type on chembl.assay_bases(type);
create index assay_bases__description on chembl.assay_bases using hash (description);
create index assay_bases__document on chembl.assay_bases(document);
create index assay_bases__target on chembl.assay_bases(target);
create index assay_bases__source on chembl.assay_bases(source);
create index assay_bases__cell_line on chembl.assay_bases(cell_line);
create index assay_bases__format on chembl.assay_bases(format_id);
create index assay_bases__organism on chembl.assay_bases(organism);
create index assay_bases__taxonomy on chembl.assay_bases(taxonomy);
create index assay_bases__category on chembl.assay_bases(category);
create index assay_bases__cell_type on chembl.assay_bases(cell_type);
create index assay_bases__strain on chembl.assay_bases(strain);
create index assay_bases__tissue on chembl.assay_bases(tissue);
create index assay_bases__subcellular_fraction on chembl.assay_bases(subcellular_fraction);
create index assay_bases__test_type on chembl.assay_bases(test_type);
create index assay_bases__relationship_type on chembl.assay_bases(relationship_type);
create index assay_bases__relationship_desc on chembl.assay_bases(relationship_desc);
create index assay_bases__confidence_score on chembl.assay_bases(confidence_score);
create index assay_bases__confidence_desc on chembl.assay_bases(confidence_desc);
create index assay_bases__pubchem_assay on chembl.assay_bases(pubchem_assay);
create index assay_bases__pubchem_bioassay on chembl.assay_bases(pubchem_bioassay);
grant select on chembl.assay_bases to sparql;

--------------------------------------------------------------------------------

create index assay_reference_labels__reference on chembl.assay_reference_labels(reference);
create index assay_reference_labels__label on chembl.assay_reference_labels(label);
grant select on chembl.assay_reference_labels to sparql;
