select sachem.add_index('wikidata', 'wikidata', 'compound_structures', 'compound', 'smiles', 16, 16, 100000, 0);
select sachem.sync_data('wikidata', false, true);
