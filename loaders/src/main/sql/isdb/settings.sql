create index compounds__inchikey on isdb.compounds(inchikey);
create index compounds__exact_mass on isdb.compounds(exact_mass);
create index compounds__formula on isdb.compounds(formula);
create index compounds__smiles on isdb.compounds(smiles);
create index compounds__inchi on isdb.compounds(inchi);
grant select on isdb.compounds to sparql;

--------------------------------------------------------------------------------

create index spectra__compound on isdb.spectra(compound);
create index spectra__ionmode on isdb.spectra(ionmode);
create index spectra__pepmass on isdb.spectra(pepmass);
grant select on isdb.spectra to sparql;
