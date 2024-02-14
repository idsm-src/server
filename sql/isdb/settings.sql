create index compound_bases__seq on isdb.compound_bases(seq);
create index compound_bases__exact_mass on isdb.compound_bases(exact_mass);
create index compound_bases__formula on isdb.compound_bases(formula);
create index compound_bases__smiles on isdb.compound_bases(smiles);
create index compound_bases__inchi on isdb.compound_bases(inchi);
grant select on isdb.compound_bases to sparql;

--------------------------------------------------------------------------------

create index spectrum_bases__id on isdb.spectrum_bases(id);
create index spectrum_bases__ionmode on isdb.spectrum_bases(ionmode);
create index spectrum_bases__pepmass on isdb.spectrum_bases(pepmass);
grant select on isdb.spectrum_bases to sparql;
