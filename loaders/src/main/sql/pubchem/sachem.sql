select sachem.add_index('pubchem', 'pubchem', 'compound_molfiles', 'compound', 'molfile', 64, 64, 100000, 0);
select sachem.sync_data('pubchem', false, false);
