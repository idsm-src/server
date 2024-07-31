create materialized view isdb.compound_pubchem_compounds(compound, cid) as
    select distinct b.id, p.compound from isdb.compound_bases b, pubchem.descriptor_compound_iupac_inchis p
        where regexp_replace(b.inchi, '/[tbms].*', '') = regexp_replace(p.iupac_inchi, '/[tbms].*', '');

create index compound_pubchem_compounds__compound on isdb.compound_pubchem_compounds(compound);
create index compound_pubchem_compounds__cid on isdb.compound_pubchem_compounds(cid);
grant select on isdb.compound_pubchem_compounds to sparql;

--------------------------------------------------------------------------------

create materialized view isdb.compound_wikidata_compounds(compound, wikidata) as
    select distinct b.id, w.compound from isdb.compound_bases b, wikidata.inchies w
        where regexp_replace(b.inchi, '/[tbms].*', '') = regexp_replace(w.inchi, '/[tbms].*', '');

create index compound_wikidata_compounds__compound on isdb.compound_wikidata_compounds(compound);
create index compound_wikidata_compounds__wikidata on isdb.compound_wikidata_compounds(wikidata);
grant select on isdb.compound_wikidata_compounds to sparql;
