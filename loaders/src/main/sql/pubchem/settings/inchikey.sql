grant select on pubchem.inchikey_bases to sparql;

--------------------------------------------------------------------------------

create index inchikey_compounds__inchikey on pubchem.inchikey_compounds(inchikey);
grant select on pubchem.inchikey_compounds to sparql;

--------------------------------------------------------------------------------

create index inchikey_subjects__subject on pubchem.inchikey_subjects(subject);
grant select on pubchem.inchikey_subjects to sparql;
