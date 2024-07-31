create index canonical_smiles__compound on wikidata.canonical_smiles(compound);
grant select on wikidata.canonical_smiles to sparql;

--------------------------------------------------------------------------------

create index isomeric_smiles__compound on wikidata.isomeric_smiles(compound);
grant select on wikidata.isomeric_smiles to sparql;

--------------------------------------------------------------------------------

create index inchies__compound on wikidata.inchies(compound);
create index inchies__inchi on wikidata.inchies(inchi);
grant select on wikidata.inchies to sparql;
