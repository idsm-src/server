create index compound_canonical_smileses__compound on wikidata.compound_canonical_smileses(compound);
grant select on wikidata.compound_canonical_smileses to sparql;

--------------------------------------------------------------------------------

create index compound_isomeric_smileses__compound on wikidata.compound_isomeric_smileses(compound);
grant select on wikidata.compound_isomeric_smileses to sparql;

--------------------------------------------------------------------------------

create index compound_inchis__compound on wikidata.compound_inchis(compound);
create index compound_inchis__inchi on wikidata.compound_inchis(inchi);
grant select on wikidata.compound_inchis to sparql;

--------------------------------------------------------------------------------

create index compound_structures__smiles on wikidata.compound_structures(smiles);
grant select on wikidata.compound_structures to sparql;
