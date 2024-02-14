create materialized view mona.compound_pubchem_compounds(compound, cid) as
    select m.compound, p.compound from mona.compound_inchis m, pubchem.descriptor_compound_iupac_inchis p
        where m.inchi = p.iupac_inchi
    union
    select compound, cid from mona.compound_pubchem_compound_ids;

create index compound_pubchem_compounds__compound on mona.compound_pubchem_compounds(compound);
create index compound_pubchem_compounds__cid on mona.compound_pubchem_compounds(cid);
grant select on mona.compound_pubchem_compounds to sparql;
