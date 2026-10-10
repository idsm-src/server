create index patentipcs__label on pubchem.patentipcs(label);
create index patentipcs__broader on pubchem.patentipcs(broader);
create index patentipcs__epo_id on pubchem.patentipcs(epo_id);
create index patentipcs__wipo_id on pubchem.patentipcs(wipo_id);
create index patentipcs__image on pubchem.patentipcs(image);
grant select on pubchem.patentipcs to sparql;

--------------------------------------------------------------------------------

create index patentipc_titles__patentipc on pubchem.patentipc_titles(patentipc);
create index patentipc_titles__title on pubchem.patentipc_titles(title);
grant select on pubchem.patentipc_titles to sparql;
