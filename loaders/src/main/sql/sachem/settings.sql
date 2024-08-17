select sachem.add_index('drugbank', 'molecules', 'drugbank', 'id', 'molfile', 4, 4, 250, 0);
grant select on table molecules.drugbank to sparql;

--------------------------------------------------------------------------------

select sachem.add_index('chebi', 'molecules', 'chebi', 'id', 'molfile', 8, 8, 1000, 0);
grant select on table molecules.chebi to sparql;

--------------------------------------------------------------------------------

select sachem.add_index('chembl', 'molecules', 'chembl', 'id', 'molfile', 16, 16, 10000, 0);
grant select on table molecules.chembl to sparql;

--------------------------------------------------------------------------------

select sachem.add_index('pubchem', 'molecules', 'pubchem', 'id', 'molfile', 64, 64, 100000, 0);
grant select on table molecules.pubchem to sparql;

--------------------------------------------------------------------------------

select sachem.add_index('wikidata', 'molecules', 'wikidata', 'id', 'smiles', 16, 16, 100000, 0);
grant select on table molecules.wikidata to sparql;
