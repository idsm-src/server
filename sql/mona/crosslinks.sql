create materialized view mona.compound_pubchem_compounds(compound, cid) as
    select m.compound, p.compound from mona.compound_inchis m, pubchem.descriptor_compound_iupac_inchis p
        where m.inchi = p.iupac_inchi
    union
    select compound, cid from mona.compound_pubchem_compound_ids;
