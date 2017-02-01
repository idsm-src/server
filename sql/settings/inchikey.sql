grant select on inchikey_bases to "SPARQL";

--------------------------------------------------------------------------------

create index inchikey_compounds__inchikey on inchikey_compounds(inchikey);
grant select on inchikey_compounds to "SPARQL";

--------------------------------------------------------------------------------

create index inchikey_subjects__subject on inchikey_subjects(subject);
grant select on inchikey_subjects to "SPARQL";
