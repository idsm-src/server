create index patents__title on pubchem.patents using hash(title);
create index patents__abstract on pubchem.patents using hash(abstract);
create index patents__publication_number on pubchem.patents(publication_number);
create index patents__filing_date on pubchem.patents(filing_date);
create index patents__grant_date on pubchem.patents(grant_date);
create index patents__publication_date on pubchem.patents(publication_date);
create index patents__priority_date on pubchem.patents(priority_date);
grant select on pubchem.patents to sparql;

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

create index patent_inventors__patent on pubchem.patent_inventors(patent);
create index patent_inventors__inventor on pubchem.patent_inventors(inventor);
grant select on pubchem.patent_inventors to sparql;

--------------------------------------------------------------------------------

create index patent_applicants__patent on pubchem.patent_applicants(patent);
create index patent_applicants__applicant on pubchem.patent_applicants(applicant);
grant select on pubchem.patent_applicants to sparql;

--------------------------------------------------------------------------------

create index inventors__name on pubchem.inventors(name);
grant select on pubchem.inventors to sparql;

--------------------------------------------------------------------------------

create index applicants__name on pubchem.applicants(name);
grant select on pubchem.applicants to sparql;
