alter table chembl_tmp.assays drop column assay_id;
alter table chembl_tmp.assays drop column doc_id;
alter table chembl_tmp.assays drop column tid;
alter table chembl_tmp.assays drop column curated_by;
alter table chembl_tmp.assays drop column src_assay_id;
alter table chembl_tmp.assays drop column cell_id;
alter table chembl_tmp.assays drop column bao_format;
alter table chembl_tmp.assays drop column tissue_id;
alter table chembl_tmp.assays drop column variant_id;
alter table chembl_tmp.assays drop column aidx;

alter table chembl_tmp.assays add primary key (id);
create index assays__description on chembl_tmp.assays(description);
create index assays__assay_type on chembl_tmp.assays(assay_type);
create index assays__assay_test_type on chembl_tmp.assays(assay_test_type);
create index assays__assay_organism on chembl_tmp.assays(assay_organism);
create index assays__assay_tax_id on chembl_tmp.assays(assay_tax_id);
create index assays__assay_strain on chembl_tmp.assays(assay_strain);
create index assays__assay_tissue on chembl_tmp.assays(assay_tissue);
create index assays__assay_cell_type on chembl_tmp.assays(assay_cell_type);
create index assays__assay_subcellular_fraction on chembl_tmp.assays(assay_subcellular_fraction);
create index assays__relationship_type on chembl_tmp.assays(relationship_type);
create index assays__confidence_score on chembl_tmp.assays(confidence_score);
create index assays__src_id on chembl_tmp.assays(src_id);
create index assays__chembl_id on chembl_tmp.assays(chembl_id);
create index assays__cell_line_id on chembl_tmp.assays(cell_line_id);
create index assays__document_id on chembl_tmp.assays(document_id);
create index assays__target_id on chembl_tmp.assays(target_id);
create index assays__bao_format_id on chembl_tmp.assays(bao_format_id);
create index assays__pubchem_assay_id on chembl_tmp.assays(pubchem_assay_id);
grant select on chembl_tmp.assays to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.confidence_score_lookup add primary key (confidence_score);
create index confidence_score_lookup__description on chembl_tmp.confidence_score_lookup(description);
create index confidence_score_lookup__target_mapping on chembl_tmp.confidence_score_lookup(target_mapping);
grant select on chembl_tmp.confidence_score_lookup to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.assay_type add primary key (assay_type);
grant select on chembl_tmp.assay_type to sparql;

--------------------------------------------------------------------------------

alter table chembl_tmp.relationship_type add primary key (relationship_type);
create index relationship_type__relationship_desc on chembl_tmp.relationship_type(relationship_desc);
grant select on chembl_tmp.relationship_type to sparql;

--------------------------------------------------------------------------------

create view chembl_tmp.pubchem_assays as
    select distinct pubchem_assay_id from chembl_tmp.assays;

grant select on chembl_tmp.pubchem_assays to sparql;
