insert into info.queries values (1,
'What protein targets does donepezil (CHEBI_53289) inhibit with an IC50 of 10 µM or lower?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>

SELECT DISTINCT ?protein ?title ?qualifier ?value
FROM pubchem:protein
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:substance
WHERE {
  ?sub a obo:CHEBI_53289 ;
    obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein ;
    obo:OBI_0000299 ?ep .
  ?protein a sio:SIO_010043 ;
    skos:prefLabel ?title .
  ?ep a bao:BAO_0000190 ;
    obo:IAO_0000136 ?sub ;
    sio:SIO_000300 ?value .
  FILTER (?value < 10)
  OPTIONAL {
    ?ep vocab:hasQualifier ?qualifier .
    FILTER (?qualifier = "=")
  }
}');

insert into info.queries values (2,
'What pharmacological roles of SID46505803 are defined by CHEBI?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX substance: <http://rdf.ncbi.nlm.nih.gov/pubchem/substance/>

SELECT DISTINCT ?rolelabel
FROM pubchem:substance
FROM pubchem:compound
FROM ebi:chebi
WHERE {
  substance:SID46505803 sio:CHEMINF_000477 ?comp .
  ?comp a ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obo:RO_0000087 ;
    owl:someValuesFrom ?role
  ] .
  ?role rdfs:label ?rolelabel .
}');

insert into info.queries values (3,
'What compounds have a pharmacological role of NSAID as defined by CHEBI and molecular weight less than 200 g/mol?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX descriptor: <http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/>

SELECT DISTINCT ?compound
FROM pubchem:compound
FROM descriptor:compound
FROM ebi:chebi
WHERE {
  ?compound a ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obo:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_35475
  ] .
  ?compound sio:SIO_000008 ?MW .
  ?MW a sio:CHEMINF_000334 .
  ?MW sio:SIO_000300 ?MWValue .
  FILTER (?MWValue < 200)
}');

insert into info.queries values (4,
'What substances have a pharmacological role of NSAID as defined by CHEBI and chemical vendor information?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX concept: <http://rdf.ncbi.nlm.nih.gov/pubchem/concept/>

SELECT DISTINCT ?substance ?source
FROM pubchem:substance
FROM pubchem:source
FROM ebi:chebi
WHERE {
  ?substance dcterms:source ?source .
  ?source dcterms:subject concept:Chemical_Vendors .
  ?substance a ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obo:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_35475
  ] .
}');

insert into info.queries values (5,
'What protein targets are inhibited by substances with an IC50 less than 10 µM and have a pharmacological role of cholinesterase inhibitors as defined by CHEBI?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?title
FROM pubchem:substance
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:protein
FROM ebi:chebi
WHERE {
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obo:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_37733
  ] .
  ?sub a ?chebi ;
    obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein ;
    obo:OBI_0000299 ?ep .
  ?protein a sio:SIO_010043 ;
    skos:prefLabel ?title .
  ?ep a bao:BAO_0000190 ;
    obo:IAO_0000136 ?sub ;
    sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.queries values (6,
'Which substances inhibit protein targets similar to ACCP00533 and have the function domain PSSMID395614?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>
PREFIX conserveddomain: <http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/>
PREFIX protein: <http://rdf.ncbi.nlm.nih.gov/pubchem/protein/>

SELECT DISTINCT ?substance ?protein ?value
FROM pubchem:substance
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:protein
FROM pubchem:conserveddomain
WHERE {
  ?substance obo:RO_0000056 ?measuregroup .
  ?measuregroup obo:RO_0000057 ?protein .
  protein:ACCP00533 vocab:hasSimilarProtein ?protein .
  ?protein obo:RO_0002180 conserveddomain:PSSMID395614 .
  ?measuregroup obo:OBI_0000299 ?endpoint .
  ?endpoint obo:IAO_0000136 ?substance .
  ?endpoint a bao:BAO_0000190 .
  ?endpoint sio:SIO_000300 ?value .
}');

insert into info.queries values (7,
'What protein targets are inhibited by substances with IC50 less than 10 µM and have the same standardized chemical structure (CID3152)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT DISTINCT ?sub ?protein ?title
FROM pubchem:protein
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:substance
WHERE {
  ?sub sio:CHEMINF_000477 compound:CID3152 ;
    obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein ;
    obo:OBI_0000299 ?ep .
  ?protein a sio:SIO_010043 ;
    skos:prefLabel ?title .
  ?ep a bao:BAO_0000190 ;
    obo:IAO_0000136 ?sub ;
    sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.queries values (8,
'What substances inhibit the proteins involved in the same biological pathway: prostaglandin biosynthetic process (GO:0001516), with an IC 50 less than 10 µM?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?substance ?protein
FROM pubchem:substance
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:protein
FROM pubchem:gene
FROM pubchem:pathway
WHERE {
  ?substance obo:RO_0000056 ?measuregroup .
  ?measuregroup obo:RO_0000057 ?protein .
  ?protein a sio:SIO_010043 .
  ?protein up:encodedBy ?gene .
  ?gene a sio:SIO_010035 .
  ?gene obo:RO_0000056 obo:GO_0001516 .
  ?measuregroup obo:OBI_0000299 ?endpoint .
  ?endpoint obo:IAO_0000136 ?substance .
  ?endpoint a bao:BAO_0000190 .
  ?endpoint sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.queries values (9,
'What the pharmacological roles defined by CHEBI are for the substances that inhibit protein target ACCQ12809 with an IC50 less than 10 µM?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX protein: <http://rdf.ncbi.nlm.nih.gov/pubchem/protein/>

SELECT DISTINCT ?rolelabel
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:substance
FROM ebi:chebi
WHERE {
  ?sub obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 protein:ACCQ12809 ;
    obo:OBI_0000299 ?ep .
  ?sub a ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obo:RO_0000087 ;
    owl:someValuesFrom ?role
  ] .
  ?role rdfs:label ?rolelabel .
  ?ep obo:IAO_0000136 ?sub ;
    a bao:BAO_0000190 ;
    sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.queries values (10,
'Summarize the statistics about the total number of substances tested in the PubChem database against each protein target.',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT (COUNT(?sub) AS ?subcnt) ?protein
FROM pubchem:substance
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:protein
WHERE {
  ?sub obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein .
  ?protein a sio:SIO_010043 .
  ?mg obo:OBI_0000299 ?ep .
  ?ep a bao:BAO_0000190 ;
    obo:IAO_0000136 ?sub ;
    sio:SIO_000300 ?value .
}
GROUP BY ?protein
ORDER BY ?subcnt');

insert into info.queries values (11,
'What are the top five diseases commonly mentioned with indomethacin (CID3715)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?disease ?score ?disease_prefLabel
FROM pubchem:cooccurrence
FROM pubchem:disease
WHERE {
  ?cooccurrence rdf:subject compound:CID3715 .
  ?cooccurrence rdf:object ?disease .
  ?cooccurrence a sio:SIO_000993 .
  ?cooccurrence sio:SIO_000300 ?score .
  ?disease skos:prefLabel ?disease_prefLabel .
}
ORDER BY DESC(?score)
LIMIT 5');

insert into info.queries values (12,
'What are the three most recent references that mention indomethacin (CID3715) and inflammation (DZID8173)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX prism: <http://prismstandard.org/namespaces/basic/3.0/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>
PREFIX disease: <http://rdf.ncbi.nlm.nih.gov/pubchem/disease/>

SELECT ?ref ?date ?journal ?title
FROM pubchem:reference
WHERE {
  ?ref vocab:discussesAsDerivedByTextMining compound:CID3715 .
  ?ref vocab:discussesAsDerivedByTextMining disease:DZID8173 .
  ?ref dcterms:date ?date .
  ?ref dcterms:title ?title .
  ?ref prism:publicationName ?journal .
}
ORDER BY DESC(?date)
LIMIT 3');

insert into info.queries values (13,
'What are the top 20 genes co-mentioned with indomethacin (CID3715)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>
PREFIX taxonomy: <http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/>

SELECT ?gene ?geneid ?prefLabel
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence rdf:subject compound:CID3715 .
  ?cooccurrence rdf:object ?gene .
  ?cooccurrence a sio:SIO_001257 .
  ?cooccurrence sio:SIO_000300 ?score .
  ?geneid bao:BAO_0002870 ?gene .
  ?geneid up:organism taxonomy:TAXID9606 .
  ?geneid skos:prefLabel ?prefLabel .
}
ORDER BY DESC(?score)
LIMIT 20');

insert into info.queries values (14,
'What are the top ten diseases co-occurring with the gene most commonly mentioned with maribavir (CID471161)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?gene ?disease ?disease_prefLabel
FROM pubchem:cooccurrence
FROM pubchem:disease
WHERE {
  {
    SELECT ?gene
    WHERE {
      ?cooccurrence1 rdf:subject compound:CID471161 .
      ?cooccurrence1 rdf:object ?gene .
      ?cooccurrence1 a sio:SIO_001257 .
      ?cooccurrence1 sio:SIO_000300 ?score1 .
    }
    ORDER BY DESC(?score1)
    LIMIT 1
  }
  ?cooccurrence2 rdf:subject ?gene .
  ?cooccurrence2 rdf:object ?disease .
  ?cooccurrence2 a sio:SIO_000983 .
  ?cooccurrence2 sio:SIO_000300 ?score2 .
  ?disease skos:prefLabel ?disease_prefLabel .
}
ORDER BY DESC(?score2)
LIMIT 10');

insert into info.queries values (15,
'What chemicals are commonly mentioned with the fibroblast growth factor receptor 1 gene (FGFR1)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?compound
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence rdf:subject ?genesymbol .
  ?genesymbol a sio:SIO_001383 ;
    sio:SIO_000300 "fgfr1" .
  ?cooccurrence rdf:object ?compound .
  ?cooccurrence a sio:SIO_001257 .
  ?cooccurrence sio:SIO_000300 ?score .
}');

insert into info.queries values (16,
'What chemicals are co-mentioned with a set of three genes, i.e., kinase insert domain receptor (KDR), platelet derived growth factor receptor beta (PDGFRB), and fibroblast growth factor receptor 1 (FGFR1)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?score1 ?score2 ?score3 ?compound
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence1 rdf:subject ?genesymbol1 .
  ?genesymbol1 a sio:SIO_001383 ;
    sio:SIO_000300 "kdr" .
  ?cooccurrence1 rdf:object ?compound .
  ?cooccurrence1 a sio:SIO_001257 .
  ?cooccurrence1 sio:SIO_000300 ?score1 .

  ?cooccurrence2 rdf:subject ?genesymbol2 .
  ?genesymbol2 a sio:SIO_001383 ;
    sio:SIO_000300 "pdgfrb" .
  ?cooccurrence2 rdf:object ?compound .
  ?cooccurrence2 a sio:SIO_001257 .
  ?cooccurrence2 sio:SIO_000300 ?score2 .

  ?cooccurrence3 rdf:subject ?genesymbol3 .
  ?genesymbol3 a sio:SIO_001383 ;
    sio:SIO_000300 "fgfr1" .
  ?cooccurrence3 rdf:object ?compound .
  ?cooccurrence3 a sio:SIO_001257 .
  ?cooccurrence3 sio:SIO_000300 ?score3 .
}
ORDER BY DESC(?score1) DESC(?score2) DESC(?score3)');

insert into info.queries values (17,
'What are the drug-likeness properties of Aspirin (CID2244) according to Lipinski''s Rule of Five?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?name ?MW ?HBD ?HBA ?LogP
FROM pubchem:compound
WHERE {
  compound:CID2244 skos:prefLabel ?name ;
    vocab:molecular_weight ?MW ;
    vocab:hydrogen_bond_donor_count ?HBD ;
    vocab:hydrogen_bond_acceptor_count ?HBA ;
    vocab:xlogp3 ?LogP .
}');

insert into info.queries values (101,
'How can I retrieve all Rhea reactions that involve L-glutamate(1-) (CID 5460299)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT DISTINCT ?rhea ?equation
WHERE {
  GRAPH pubchem:compound {
    compound:CID5460299 a ?chebi .
  }

  SERVICE <https://sparql.rhea-db.org/sparql> {
    ?rhea rdfs:subClassOf rh:Reaction .
    ?rhea rh:equation ?equation .
    ?rhea rh:side/rh:contains/rh:compound ?compound .

    ?compound (rh:chebi|(rh:reactivePart/rh:chebi)|(rh:underlyingChebi/rh:chebi)) ?chebi .
  }
}');

insert into info.queries values (102,
'How can I retrieve all compounds involved in the given Rhea reaction (RHEA:10020)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rh: <http://rdf.rhea-db.org/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?compound
WHERE {
  SERVICE <https://sparql.rhea-db.org/sparql> {
    VALUES ?rhea { rh:10020 }

    ?rhea rdfs:subClassOf rh:Reaction .
    ?rhea rh:equation ?equation .
    ?rhea rh:side/rh:contains/rh:compound ?cmpd .

    ?cmpd (rh:chebi|(rh:reactivePart/rh:chebi)|(rh:underlyingChebi/rh:chebi)) ?chebi .
  }

  GRAPH pubchem:compound {
    ?compound a ?chebi .
  }
}');

insert into info.queries values (103,
'How can I retrieve the WURCS sequence from Glycosmos for the glycan structure (SID 252275760) in PubChem?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX substance: <http://rdf.ncbi.nlm.nih.gov/pubchem/substance/>

SELECT DISTINCT ?gtcid ?wurcs
WHERE {
  GRAPH pubchem:substance {
    substance:SID252275760 rdfs:seeAlso ?glycan .
  }

  SERVICE <https://ts.glycosmos.org/sparql> {
    ?glycan dcterms:identifier ?gtcid ;
      glycan:has_glycosequence ?gs .
    ?gs glycan:in_carbohydrate_format glycan:carbohydrate_format_wurcs ;
      glycan:has_sequence ?wurcs .
  }
}');

insert into info.queries values (104,
'Which PubChem pathways include the genes associated with Keshan disease (DOID:0050083), as identified by Glycosmos?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?pathway ?title
WHERE {
  SERVICE <https://ts.glycosmos.org/sparql> {
    VALUES ?disease { <http://glycosmos.org/disease/DOID:0050083> }

    ?disease a glycan:Disease ;
      sio:SIO_000255 [ sio:SIO_000001 ?glycogene ] .
  }

  GRAPH pubchem:gene {
    ?gene a sio:SIO_010035 ;
      rdfs:seeAlso ?glycogene .
  }

  GRAPH pubchem:pathway {
    ?pathway a bp3:Pathway ;
      obo:RO_0000057 ?gene ;
      dcterms:title ?title .
  }
}');

insert into info.queries values (105,
'Which PDB structures with a resolution better than 2 Å that include Aspirin (CID 2244) and have associated bioactivity data in PubChem?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?structure ?resolution
WHERE {
  GRAPH pubchem:substance {
    ?substance cheminf:CHEMINF_000477 compound:CID2244 ;
      pdbo:link_to_pdb ?structure .
  }

  SERVICE <https://sparql.uniprot.org/sparql> {
    ?structure a up:Structure_Resource ;
      up:database <http://purl.uniprot.org/database/PDB> ;
      up:resolution ?resolution .
    FILTER (?resolution < 2)

    ?uniprot a up:Protein ;
      rdfs:seeAlso ?structure .
  }

  GRAPH pubchem:protein {
    ?protein a sio:SIO_010043 ;
      rdfs:seeAlso ?uniprot .
  }

  FILTER EXISTS {
    GRAPH pubchem:measuregroup {
      ?measuregroup a bao:BAO_0000040 ;
        obo:RO_0000057 ?protein .
    }
  }
}');

insert into info.queries values (106,
'How can I retrieve all compounds involved in PDB structures with a resolution better than 2 Å for the protein Basic phospholipase A2 VRV-PL-VIIIa (UniProt ID: P59071)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?compound
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql> {
    VALUES ?uniprot { <http://purl.uniprot.org/uniprot/P59071> }

    ?uniprot a up:Protein ;
      rdfs:seeAlso ?pdb .

    ?pdb a up:Structure_Resource ;
      up:database <http://purl.uniprot.org/database/PDB> ;
      up:resolution ?resolution .
    FILTER (?resolution < 2)
  }

  GRAPH pubchem:substance {
    ?substance cheminf:CHEMINF_000477 ?compound ;
      pdbo:link_to_pdb ?pdb .
  }
}');

insert into info.queries values (107,
'How to retrieve the labels of Aspirin (CID 2244) in English and Spanish from Wikidata?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?wdent ?label
WHERE {
  GRAPH pubchem:compound {
    compound:CID2244 rdfs:seeAlso ?wdent .
  }

  SERVICE <https://query.wikidata.org/sparql> {
    ?wdent rdfs:label ?label .
    FILTER (LANG(?label) = "en" || LANG(?label) = "es")
  }
}');

insert into info.queries values (108,
'How to retrieve the preferred label from PubChem for the Wikidata entry (Q18216)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX wd: <http://www.wikidata.org/entity/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?label
WHERE {
  SERVICE <https://query.wikidata.org/sparql> {
    VALUES ?wdent { wd:Q18216 }

    ?wdent wdt:P662 ?cid .
  }

  GRAPH pubchem:compound {
    ?compound dcterms:identifier ?cid ;
      skos:prefLabel ?label .
  }
}');

insert into info.queries values (109,
'How can I find the WikiPathways that include the compound dihydroflavine-adenine dinucleotide (CID 446013)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT DISTINCT ?wpid
WHERE {
  GRAPH pubchem:pathway {
    ?pathway a bp3:Pathway ;
      rdfs:seeAlso ?wp ;
      obo:RO_0000057 compound:CID446013 .
    FILTER (CONTAINS(STR(?wp), "wikipathways"))
  }

  BIND (IRI(REPLACE(STR(?wp), "http://identifiers.org/wikipathways:",
    "https://identifiers.org/wikipathways/")) AS ?wikipathways)

  SERVICE <https://sparql.wikipathways.org/sparql> {
    ?wikipathways a wp:Pathway ;
      dcterms:identifier ?wpid .
  }
}');

insert into info.queries values (110,
'How can I retrieve the CID of compounds involved in the pathway Electron Transport Chain: OXPHOS system in mitochondria (Wikipathways:WP111)?',
'https://idsm.elixir-czech.cz/sparql/endpoint/idsm',
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?cid
WHERE {
  SERVICE <https://sparql.wikipathways.org/sparql> {
    ?wikipathways a wp:Pathway ;
      dcterms:identifier "WP111" .
  }

  SERVICE <https://sparql.api.identifiers.org/sparql> {
    GRAPH <id:active> {
      ?wikipathways owl:sameAs ?wp .
    }
  }

  GRAPH pubchem:pathway {
    ?pathway a bp3:Pathway ;
      rdfs:seeAlso ?wp ;
      obo:RO_0000057 ?cmpd .
  }

  GRAPH pubchem:compound {
    ?cmpd dcterms:identifier ?cid .
  }
}');
