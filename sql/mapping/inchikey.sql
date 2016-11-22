sparql
alter quad storage virtrdf:PubchemQuadStorage
    from DB.rdf.inchikey_bases     as inchikey_bases
    from DB.rdf.inchikey_compounds as inchikey_compounds
    from DB.rdf.inchikey_subjects  as inchikey_subjects
{
    create map:inchikey as graph pubchem:inchikey option (exclusive)
    {
        iri:inchikey(inchikey_bases.id)
            rdf:type sio:CHEMINF_000399 ;
            sio:has-value inchikey_bases.inchikey .

        iri:inchikey(inchikey_compounds.inchikey)
            sio:is-attribute-of iri:substance(inchikey_compounds.compound) .

        iri:inchikey(inchikey_subjects.inchikey)
            dcterms:subject iri:inchikey_subject(inchikey_subjects.subject) .
    }.
}.;
