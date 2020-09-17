alter table chembl.assays drop column assay_id;
alter table chembl.assays drop column doc_id;
alter table chembl.assays drop column tid;
alter table chembl.assays drop column curated_by;
alter table chembl.assays drop column activity_count;
alter table chembl.assays drop column assay_source;
alter table chembl.assays drop column src_assay_id;
alter table chembl.assays drop column updated_on;
alter table chembl.assays drop column updated_by;
alter table chembl.assays drop column orig_description;
alter table chembl.assays drop column mc_tax_id;
alter table chembl.assays drop column mc_organism;
alter table chembl.assays drop column mc_target_type;
alter table chembl.assays drop column mc_target_name;
alter table chembl.assays drop column mc_target_accession;
alter table chembl.assays drop column cell_id;
alter table chembl.assays drop column bao_format;
alter table chembl.assays drop column tissue_id;
alter table chembl.assays drop column curation_comment;
alter table chembl.assays drop column variant_id;
alter table chembl.assays drop column aidx;
alter table chembl.assays drop column job_id;
alter table chembl.assays drop column log_id;
alter table chembl.assays drop column ridx;
alter table chembl.assays drop column tid_fixed;

alter table chembl.assays add primary key (id);
create index assays__description on chembl.assays(description);
create index assays__assay_type on chembl.assays(assay_type);
create index assays__assay_test_type on chembl.assays(assay_test_type);
create index assays__assay_organism on chembl.assays(assay_organism);
create index assays__assay_tax_id on chembl.assays(assay_tax_id);
create index assays__assay_strain on chembl.assays(assay_strain);
create index assays__assay_tissue on chembl.assays(assay_tissue);
create index assays__assay_cell_type on chembl.assays(assay_cell_type);
create index assays__assay_subcellular_fraction on chembl.assays(assay_subcellular_fraction);
create index assays__relationship_type on chembl.assays(relationship_type);
create index assays__confidence_score on chembl.assays(confidence_score);
create index assays__src_id on chembl.assays(src_id);
create index assays__chembl_id on chembl.assays(chembl_id);
create index assays__cell_line_id on chembl.assays(cell_line_id);
create index assays__document_id on chembl.assays(document_id);
create index assays__target_id on chembl.assays(target_id);
create index assays__bao_format_id on chembl.assays(bao_format_id);
create index assays__pubchem_assay_id on chembl.assays(pubchem_assay_id);
grant select on chembl.assays to sparql;

--------------------------------------------------------------------------------

alter table chembl.confidence_score_lookup add primary key (confidence_score);
create index confidence_score_lookup__description on chembl.confidence_score_lookup(description);
create index confidence_score_lookup__target_mapping on chembl.confidence_score_lookup(target_mapping);
grant select on chembl.confidence_score_lookup to sparql;

--------------------------------------------------------------------------------

alter table chembl.assay_type add primary key (assay_type);
grant select on chembl.assay_type to sparql;

--------------------------------------------------------------------------------

alter table chembl.relationship_type add primary key (relationship_type);
create index relationship_type__relationship_desc on chembl.relationship_type(relationship_desc);
grant select on chembl.relationship_type to sparql;
