create index patent_bases__title on pubchem.patent_bases using hash(title);
create index patent_bases__abstract on pubchem.patent_bases using hash(abstract);
create index patent_bases__publication_number on pubchem.patent_bases(publication_number);
create index patent_bases__filing_date on pubchem.patent_bases(filing_date);
create index patent_bases__grant_date on pubchem.patent_bases(grant_date);
create index patent_bases__publication_date on pubchem.patent_bases(publication_date);
create index patent_bases__priority_date on pubchem.patent_bases(priority_date);
grant select on pubchem.patent_bases to sparql;

--------------------------------------------------------------------------------

create index patent_cpc_additional_classifications__patent on pubchem.patent_cpc_additional_classifications(patent);
create index patent_cpc_additional_classifications__classification on pubchem.patent_cpc_additional_classifications(classification);
grant select on pubchem.patent_cpc_additional_classifications to sparql;

--------------------------------------------------------------------------------

create index patent_cpc_inventive_classifications__patent on pubchem.patent_cpc_inventive_classifications(patent);
create index patent_cpc_inventive_classifications__classification on pubchem.patent_cpc_inventive_classifications(classification);
grant select on pubchem.patent_cpc_inventive_classifications to sparql;

--------------------------------------------------------------------------------

create index patent_ipc_additional_classifications__patent on pubchem.patent_ipc_additional_classifications(patent);
create index patent_ipc_additional_classifications__classification on pubchem.patent_ipc_additional_classifications(classification);
grant select on pubchem.patent_ipc_additional_classifications to sparql;

--------------------------------------------------------------------------------

create index patent_ipc_inventive_classifications__patent on pubchem.patent_ipc_inventive_classifications(patent);
create index patent_ipc_inventive_classifications__classification on pubchem.patent_ipc_inventive_classifications(classification);
grant select on pubchem.patent_ipc_inventive_classifications to sparql;

--------------------------------------------------------------------------------

create index patent_citations__patent on pubchem.patent_citations(patent);
create index patent_citations__citation on pubchem.patent_citations(citation);
grant select on pubchem.patent_citations to sparql;

--------------------------------------------------------------------------------

create index patent_substances__patent on pubchem.patent_substances(patent);
create index patent_substances__substance on pubchem.patent_substances(substance);
grant select on pubchem.patent_substances to sparql;

--------------------------------------------------------------------------------

create index patent_compounds__patent on pubchem.patent_compounds(patent);
create index patent_compounds__compound on pubchem.patent_compounds(compound);
grant select on pubchem.patent_compounds to sparql;

--------------------------------------------------------------------------------

create index patent_genes__patent on pubchem.patent_genes(patent);
create index patent_genes__gene on pubchem.patent_genes(gene);
grant select on pubchem.patent_genes to sparql;

--------------------------------------------------------------------------------

create index patent_proteins__patent on pubchem.patent_proteins(patent);
create index patent_proteins__protein on pubchem.patent_proteins(protein);
grant select on pubchem.patent_proteins to sparql;

--------------------------------------------------------------------------------

create index patent_taxonomies__patent on pubchem.patent_taxonomies(patent);
create index patent_taxonomies__taxonomy on pubchem.patent_taxonomies(taxonomy);
grant select on pubchem.patent_taxonomies to sparql;

--------------------------------------------------------------------------------

create index patent_anatomies__patent on pubchem.patent_anatomies(patent);
create index patent_anatomies__anatomy on pubchem.patent_anatomies(anatomy);
grant select on pubchem.patent_anatomies to sparql;

--------------------------------------------------------------------------------

create index patent_inventors__patent on pubchem.patent_inventors(patent);
create index patent_inventors__inventor on pubchem.patent_inventors(inventor);
grant select on pubchem.patent_inventors to sparql;

--------------------------------------------------------------------------------

create index patent_applicants__patent on pubchem.patent_applicants(patent);
create index patent_applicants__applicant on pubchem.patent_applicants(applicant);
grant select on pubchem.patent_applicants to sparql;

--------------------------------------------------------------------------------

create index patent_inventor_names__inventor on pubchem.patent_inventor_names(inventor);
create index patent_inventor_names__formatted_name on pubchem.patent_inventor_names(formatted_name);
grant select on pubchem.patent_inventor_names to sparql;

--------------------------------------------------------------------------------

create index patent_applicant_names__applicant on pubchem.patent_applicant_names(applicant);
create index patent_applicant_names__formatted_name on pubchem.patent_applicant_names(formatted_name);
grant select on pubchem.patent_applicant_names to sparql;
