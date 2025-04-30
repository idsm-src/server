create table info.idsm_sources
(
    id          integer not null,
    name        varchar unique not null,
    url         varchar not null,
    version     varchar not null,
    primary key(id)
);


create table info.idsm_counts
(
    id          integer not null,
    name        varchar unique not null,
    count       integer not null,
    primary key(id)
);


create table info.idsm_queries
(
    id          integer not null,
    comment     varchar not null,
    query       varchar not null,
    primary key(id)
);


create table info.idsm_version
(
    date        timestamptz not null
);


create table info.sachem_sources (
    index       varchar not null,
    name        varchar not null,
    size        bigint not null,
    timestamp   timestamptz,
    primary key(index, name)
);


create table info.sachem_stats (
    index       varchar not null,
    version     varchar,
    checkdate   timestamptz not null,
    primary key(index)
);

--============================================================================--

insert into info.idsm_version(date) values (now());


insert into info.idsm_sources values (0, 'PubChemRDF', 'https://pubchemdocs.ncbi.nlm.nih.gov/rdf', '');
insert into info.idsm_sources values (1, 'ChEMBL', 'https://www.ebi.ac.uk/chembl/', '');
insert into info.idsm_sources values (2, 'ChEBI Ontology', 'https://www.ebi.ac.uk/chebi/', '');
insert into info.idsm_sources values (3, 'MassBank of North America (MoNA)', 'https://mona.fiehnlab.ucdavis.edu/', '');
insert into info.idsm_sources values (4, 'In Silico Spectral Database (ISDB)', 'https://zenodo.org/records/8287341', '');
insert into info.idsm_sources values (5, 'Medical Subject Headings (MESH)', 'https://id.nlm.nih.gov/mesh/', '');
insert into info.idsm_sources values (6, 'BioAssay Ontology (BAO)', 'http://bioassayontology.org/bioassayontology/', '');
insert into info.idsm_sources values (7, 'Protein Ontology (PRO)', 'https://proconsortium.org', '');
insert into info.idsm_sources values (8, 'Gene Ontology (GO)', 'http://geneontology.org', '');
insert into info.idsm_sources values (9, 'Sequence Ontology (SO)', 'http://www.sequenceontology.org', '');
insert into info.idsm_sources values (10, 'Cell Line Ontology (CLO)', 'http://www.clo-ontology.org', '');
insert into info.idsm_sources values (11, 'Cell Ontology (CL)', 'https://obophenotype.github.io/cell-ontology/', '');
insert into info.idsm_sources values (12, 'The BRENDA Tissue Ontology (BTO)', 'https://www.brenda-enzymes.org', '');
insert into info.idsm_sources values (13, 'Human Disease Ontology (DO)', 'https://disease-ontology.org', '');
insert into info.idsm_sources values (14, 'Mondo Disease Ontology (MONDO)', 'http://obofoundry.org/ontology/mondo.html', '');
insert into info.idsm_sources values (15, 'Symptom Ontology (SYMP)', 'http://symptomontologywiki.igs.umaryland.edu/mediawiki/index.php', '');
insert into info.idsm_sources values (16, 'Pathogen Transmission Ontology (TRANS)', 'https://github.com/DiseaseOntology/PathogenTransmissionOntology', '');
insert into info.idsm_sources values (17, 'The Human Phenotype Ontology (HP)', 'http://www.human-phenotype-ontology.org', '');
insert into info.idsm_sources values (18, 'Phenotype And Trait Ontology (PATO)', 'https://github.com/pato-ontology/pato/', '');
insert into info.idsm_sources values (19, 'Units of Measurement Ontology (UO)', 'https://github.com/bio-ontology-research-group/unit-ontology', '');
insert into info.idsm_sources values (20, 'Ontology for Biomedical Investigations (OBI)', 'http://obi-ontology.org', '');
insert into info.idsm_sources values (21, 'Information Artifact Ontology (IAO)', 'https://github.com/information-artifact-ontology/IAO/', '');
insert into info.idsm_sources values (22, 'Uber-anatomy Ontology (UBERON)', 'http://obophenotype.github.io/uberon/', '');
insert into info.idsm_sources values (23, 'NCBI Taxonomy Database', 'https://www.ncbi.nlm.nih.gov/taxonomy', '');
insert into info.idsm_sources values (24, 'National Center Institute Thesaurus (OBO Edition)', 'https://github.com/NCI-Thesaurus/thesaurus-obo-edition', '');
insert into info.idsm_sources values (25, 'OBO Relations Ontology', 'https://oborel.github.io', '');
insert into info.idsm_sources values (26, 'Basic Formal Ontology (BFO)', 'https://basic-formal-ontology.org', '');
insert into info.idsm_sources values (27, 'Food Ontology (FOODON)', 'http://foodon.org', '');
insert into info.idsm_sources values (28, 'Evidence and Conclusion Ontology (ECO)', 'http://evidenceontology.org', '');
insert into info.idsm_sources values (29, 'Disease Drivers Ontology (DISDRIV)', 'http://www.disease-ontology.org', '');
insert into info.idsm_sources values (30, 'Genotype Ontology (GENO)', 'https://github.com/monarch-initiative/GENO-ontology/', '');
insert into info.idsm_sources values (31, 'Common Anatomy Reference Ontology (CARO)', 'https://github.com/obophenotype/caro/', '');
insert into info.idsm_sources values (32, 'Environment Ontology (ENVO)', 'http://environmentontology.org', '');
insert into info.idsm_sources values (33, 'Ontology for General Medical Science (OGMS)', 'https://github.com/OGMS/ogms', '');
insert into info.idsm_sources values (34, 'Unified phenotype ontology (uPheno)', 'https://github.com/obophenotype/upheno', '');
insert into info.idsm_sources values (35, 'OBO Metadata Ontology', 'https://github.com/information-artifact-ontology/ontology-metadata', '');
insert into info.idsm_sources values (36, 'Biological Pathway Exchange (BioPAX)', 'http://www.biopax.org/', '');
insert into info.idsm_sources values (37, 'UniProt RDF schema ontology', 'https://www.uniprot.org', '');
insert into info.idsm_sources values (38, 'PDBx ontology', 'https://pdbj.org/', '');
insert into info.idsm_sources values (39, 'Quantities, Units, Dimensions and Types Ontology (QUDT)', 'http://qudt.org', '');
insert into info.idsm_sources values (40, 'Open PHACTS Units extending QUDT', 'http://www.openphacts.org/specs/units/', '');
insert into info.idsm_sources values (41, 'Shapes Constraint Language (SHACL)', 'https://www.w3.org/TR/shacl/', '');
insert into info.idsm_sources values (42, 'Linked Models: Datatype Ontology (DTYPE)', 'http://www.linkedmodel.org/', '');
insert into info.idsm_sources values (43, 'Linked Models: Vocabulary for Attaching Essential Metadata (VAEM)', 'http://www.linkedmodel.org/', '');
insert into info.idsm_sources values (44, 'Chemical Information Ontology (CHEMINF)', 'http://semanticchemistry.github.io/semanticchemistry/', '');
insert into info.idsm_sources values (45, 'Semanticscience integrated ontology (SIO)', 'https://sio.semanticscience.org/', '');
insert into info.idsm_sources values (46, 'Ontology of Bioscientific Data Analysis and Data Management (EDAM)', 'https://edamontology.org/', '');
insert into info.idsm_sources values (47, 'National Drug File-Reference Terminology (NDF-RT)', 'https://www.oit.va.gov/Services/TRM/StandardPage.aspx?tid=5221', '');
insert into info.idsm_sources values (48, 'National Center Institute Thesaurus (NCIt)', 'http://ncit.nci.nih.gov/', '');
insert into info.idsm_sources values (49, 'Experimental Factor Ontology (EFO)', 'https://www.ebi.ac.uk/efo/', '');
insert into info.idsm_sources values (50, 'Eagle-i Resource Ontology (ERO)', 'https://www.eagle-i.net/', '');
insert into info.idsm_sources values (51, 'Funding, Research Administration and Projects Ontology (FRAPO)', 'http://purl.org/cerif/frapo', '');
insert into info.idsm_sources values (52, 'Patent Ontology (EPO)', 'https://data.epo.org/linked-data/', '');
insert into info.idsm_sources values (53, 'W3C PROVenance Interchange', 'http://www.w3.org/TR/prov-overview/', '');
insert into info.idsm_sources values (54, 'Metadata Authority Description Schema in RDF (MADS/RDF)', 'http://www.loc.gov/standards/mads/rdf/', '');
insert into info.idsm_sources values (55, 'Citation Typing Ontology (CiTO)', 'https://sparontologies.github.io/cito/current/cito.html', '');
insert into info.idsm_sources values (56, 'Ontology for vCard', 'https://www.w3.org/TR/vcard-rdf/', '');
insert into info.idsm_sources values (57, 'Feature Annotation Location Description Ontology (FALDO)', 'http://biohackathon.org/resource/faldo', '');
insert into info.idsm_sources values (58, 'FRBR-aligned Bibliographic Ontology (FaBiO)', 'https://sparontologies.github.io/fabio/current/fabio.html', '');
insert into info.idsm_sources values (59, 'Essential FRBR in OWL2 DL Ontology (FRBR)', 'https://sparontologies.github.io/frbr/current/frbr.html', '');
insert into info.idsm_sources values (60, 'Dublin Core Metadata Initiative Terms (DCMI)', 'https://dublincore.org/specifications/dublin-core/dcmi-terms/', '');
insert into info.idsm_sources values (61, 'Bibliographic Ontology (BIBO)', 'https://www.dublincore.org/specifications/bibo/', '');
insert into info.idsm_sources values (62, 'Simple Knowledge Organization System (SKOS)', 'https://www.w3.org/2009/08/skos-reference/skos.html', '');
insert into info.idsm_sources values (63, 'Description of a Project Vocabulary (DOAP)', 'https://github.com/ewilderj/doap/wiki', '');
insert into info.idsm_sources values (64, 'FOAF Vocabulary', 'http://xmlns.com/foaf/0.1/', '');
insert into info.idsm_sources values (65, 'Provenance, Authoring and Versioning (PAV)', 'http://pav-ontology.github.io/pav/', '');
insert into info.idsm_sources values (66, 'SemWeb Vocab Status Ontology', 'https://www.w3.org/2003/06/sw-vocab-status/note.html', '');
insert into info.idsm_sources values (67, 'Vocabulary of Interlinked Datasets (VoID)', 'http://vocab.deri.ie/void.html', '');
insert into info.idsm_sources values (68, 'Situation Ontology', 'http://ontologydesignpatterns.org/wiki/Submissions:Situation', '');
insert into info.idsm_sources values (69, 'Mass Spectrometry Ontology (MS)', 'http://www.psidev.info/groups/controlled-vocabularies', '');
insert into info.idsm_sources values (70, 'ClassyFire Ontology', 'http://classyfire.wishartlab.com/', '');
insert into info.idsm_sources values (71, 'OWL 2 Schema (OWL 2)', 'https://www.w3.org/TR/owl2-overview/', '');
insert into info.idsm_sources values (72, 'RDF Schema (RDFS)', 'https://www.w3.org/TR/rdf-schema/', '');
insert into info.idsm_sources values (73, 'RDF Vocabulary Terms', 'https://www.w3.org/TR/rdf11-concepts/', '');


insert into info.idsm_counts values (0, 'PubChem Substances', 0);
insert into info.idsm_counts values (1, 'PubChem Compounds', 0);
insert into info.idsm_counts values (2, 'PubChem BioAssays', 0);
insert into info.idsm_counts values (3, 'ChEMBL Substances', 0);
insert into info.idsm_counts values (4, 'ChEMBL Assays', 0);
insert into info.idsm_counts values (5, 'ChEBI Entities', 0);


insert into info.idsm_queries values (1, 'What protein targets does donepezil (CHEBI_53289) inhibit with an IC50 less than 10 µM?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT ?protein ?title
FROM pubchem:protein
FROM pubchem:measuregroup
FROM pubchem:endpoint
FROM pubchem:substance
WHERE {
  ?sub rdf:type obo:CHEBI_53289 ;
       obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein ;
      obo:OBI_0000299 ?ep .
  ?protein rdf:type sio:SIO_010043 ;
           skos:prefLabel ?title .
  ?ep rdf:type bao:BAO_0000190 ;
      obo:IAO_0000136 ?sub ;
      sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.idsm_queries values (2, 'What pharmacological roles of SID46505803 are defined by CHEBI?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obov: <http://purl.obolibrary.org/obo/>
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
  ?comp rdf:type ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obov:RO_0000087 ;
    owl:someValuesFrom ?role
  ] .
  ?role rdfs:label ?rolelabel .
}');

insert into info.idsm_queries values (3, 'What compounds have a pharmacological role of NSAID as defined by CHEBI and molecular weight less than 200 g/mol?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX obov: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX ebi: <http://rdf.ebi.ac.uk/dataset/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX descriptor: <http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/>

SELECT DISTINCT ?compound
FROM pubchem:compound
FROM descriptor:compound
FROM ebi:chebi
WHERE {
  ?compound rdf:type ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obov:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_35475
  ] .
  ?compound sio:SIO_000008 ?MW .
  ?MW rdf:type sio:CHEMINF_000334 .
  ?MW sio:SIO_000300 ?MWValue .
  FILTER (?MWValue < 200)
}');

insert into info.idsm_queries values (4, 'What substances have a pharmacological role of NSAID as defined by CHEBI and chemical vendor information?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX obov: <http://purl.obolibrary.org/obo/>
PREFIX dcterms: <http://purl.org/dc/terms/>
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
  ?substance rdf:type ?chebi .
  ?chebi rdfs:subClassOf [
    a owl:Restriction ;
    owl:onProperty obov:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_35475
  ] .
}');

insert into info.idsm_queries values (5, 'What protein targets are inhibited by substances with an IC50 less than 10 µM and have a pharmacological role of cholinesterase inhibitors as defined by CHEBI?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX obov: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
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
    owl:onProperty obov:RO_0000087 ;
    owl:someValuesFrom obo:CHEBI_37733
  ] .
  ?sub rdf:type ?chebi ;
       obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 ?protein ;
      obo:OBI_0000299 ?ep .
  ?protein rdf:type sio:SIO_010043 ;
           skos:prefLabel ?title .
  ?ep rdf:type bao:BAO_0000190 ;
      obo:IAO_0000136 ?sub ;
      sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.idsm_queries values (6, 'Which substances inhibit protein targets similar to ACCP00533 and have the function domain PSSMID395614?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX protein: <http://rdf.ncbi.nlm.nih.gov/pubchem/protein/>
PREFIX conserveddomain: <http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>

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
  ?endpoint rdf:type bao:BAO_0000190 .
  ?endpoint sio:SIO_000300 ?value .
}');

insert into info.idsm_queries values (7, 'What protein targets are inhibited by substances with IC50 less than 10 µM and have the same standardized chemical structure (CID3152)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

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
  ?protein rdf:type sio:SIO_010043 ;
           skos:prefLabel ?title .
  ?ep rdf:type bao:BAO_0000190 ;
      obo:IAO_0000136 ?sub ;
      sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.idsm_queries values (8, 'What substances inhibit the proteins involved in the same biological pathway: prostaglandin biosynthetic process (GO:0001516), with an IC 50 less than 10 µM?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
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
  ?protein rdf:type sio:SIO_010043 .
  ?protein up:encodedBy ?gene .
  ?gene rdf:type sio:SIO_010035 .
  ?gene obo:RO_0000056 obo:GO_0001516 .
  ?measuregroup obo:OBI_0000299 ?endpoint .
  ?endpoint obo:IAO_0000136 ?substance .
  ?endpoint rdf:type bao:BAO_0000190 .
  ?endpoint sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.idsm_queries values (9, 'What the pharmacological roles defined by CHEBI are for the substances that inhibit protein target ACCQ12809 with an IC50 less than 10 µM?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
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
FROM <http://purl.obolibrary.org/obo>
WHERE {
  ?sub obo:RO_0000056 ?mg .
  ?mg obo:RO_0000057 protein:ACCQ12809 ;
      obo:OBI_0000299 ?ep .
  ?sub rdf:type ?chebi .
  ?chebi rdfs:subClassOf _:I .
  _:I a owl:Restriction ;
      owl:onProperty obo:RO_0000087 ;
      owl:someValuesFrom ?role .
  ?role rdfs:label ?rolelabel .
  ?ep obo:IAO_0000136 ?sub ;
      rdf:type bao:BAO_0000190 ;
      sio:SIO_000300 ?value .
  FILTER (?value < 10)
}');

insert into info.idsm_queries values (10, 'Summarize the statistics about the total number of substances tested in the PubChem database against each protein target.', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
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
  ?protein rdf:type sio:SIO_010043 .
  ?mg obo:OBI_0000299 ?ep .
  ?ep rdf:type bao:BAO_0000190 ;
      obo:IAO_0000136 ?sub ;
      sio:SIO_000300 ?value .
}
GROUP BY ?protein
ORDER BY ?subcnt');

insert into info.idsm_queries values (11, 'What are the top five diseases commonly mentioned with indomethacin (CID3715)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?disease ?score ?disease_prefLabel
FROM pubchem:cooccurrence
FROM pubchem:disease
WHERE {
    ?cooccurrence rdf:subject compound:CID3715 .
    ?cooccurrence rdf:object ?disease .
    ?cooccurrence rdf:type sio:SIO_000993 .
    ?cooccurrence sio:SIO_000300 ?score .
    ?disease skos:prefLabel ?disease_prefLabel .
}
ORDER BY DESC(?score)
LIMIT 5');

insert into info.idsm_queries values (12, 'What are the three most recent references that mention indomethacin (CID3715) and inflammation (DZID8173)?', 
'PREFIX prism: <http://prismstandard.org/namespaces/basic/3.0/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX vocab: <http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>
PREFIX disease: <http://rdf.ncbi.nlm.nih.gov/pubchem/disease/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

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

insert into info.idsm_queries values (13, 'What are the top 20 genes co-mentioned with indomethacin (CID3715)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>

SELECT ?gene ?geneid ?prefLabel
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence rdf:subject compound:CID3715 .
  ?cooccurrence rdf:object ?gene .
  ?cooccurrence rdf:type sio:SIO_001257 .
  ?cooccurrence sio:SIO_000300 ?score .
  ?geneid <http://www.bioassayontology.org/bao#BAO_0002870> ?gene .
  ?geneid <http://purl.uniprot.org/core/organism> <http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID9606> .
  ?geneid <http://www.w3.org/2004/02/skos/core#prefLabel> ?prefLabel .
}
ORDER BY DESC(?score)
LIMIT 20');

insert into info.idsm_queries values (14, 'What are the top ten diseases co-occurring with the gene most commonly mentioned with maribavir (CID471161)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
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
      ?cooccurrence1 rdf:type sio:SIO_001257 .
      ?cooccurrence1 sio:SIO_000300 ?score1 .
    }
    ORDER BY DESC(?score1)
    LIMIT 1
  }
  ?cooccurrence2 rdf:subject ?gene .
  ?cooccurrence2 rdf:object ?disease .
  ?cooccurrence2 rdf:type sio:SIO_000983 .
  ?cooccurrence2 sio:SIO_000300 ?score2 .
  ?disease skos:prefLabel ?disease_prefLabel .
}
ORDER BY DESC(?score2)
LIMIT 10');

insert into info.idsm_queries values (15, 'What chemicals are commonly mentioned with the fibroblast growth factor receptor 1 gene (FGFR1)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX gene: <http://rdf.ncbi.nlm.nih.gov/pubchem/gene/>

SELECT ?compound
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence rdf:subject ?genesymbol .
  ?genesymbol rdf:type sio:SIO_001383 ;
              sio:SIO_000300 "fgfr1" .
  ?cooccurrence rdf:object ?compound .
  ?cooccurrence rdf:type sio:SIO_001257 .
  ?cooccurrence sio:SIO_000300 ?score .
}');

insert into info.idsm_queries values (16, 'What chemicals are co-mentioned with a set of three genes, i.e., kinase insert domain receptor (KDR), platelet derived growth factor receptor beta (PDGFRB), and fibroblast growth factor receptor 1 (FGFR1)?', 
'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>
PREFIX gene: <http://rdf.ncbi.nlm.nih.gov/pubchem/gene/>

SELECT ?score1 ?score2 ?score3 ?compound
FROM pubchem:cooccurrence
FROM pubchem:gene
WHERE {
  ?cooccurrence1 rdf:subject ?genesymbol1 .
  ?genesymbol1 rdf:type sio:SIO_001383 ;
               sio:SIO_000300 "kdr" .
  ?cooccurrence1 rdf:object ?compound .
  ?cooccurrence1 rdf:type sio:SIO_001257 .
  ?cooccurrence1 sio:SIO_000300 ?score1 .

  ?cooccurrence2 rdf:subject ?genesymbol2 .
  ?genesymbol2 rdf:type sio:SIO_001383 ;
               sio:SIO_000300 "pdgfrb" .
  ?cooccurrence2 rdf:object ?compound .
  ?cooccurrence2 rdf:type sio:SIO_001257 .
  ?cooccurrence2 sio:SIO_000300 ?score2 .

  ?cooccurrence3 rdf:subject ?genesymbol3 .
  ?genesymbol3 rdf:type sio:SIO_001383 ;
               sio:SIO_000300 "fgfr1" .
  ?cooccurrence3 rdf:object ?compound .
  ?cooccurrence3 rdf:type sio:SIO_001257 .
  ?cooccurrence3 sio:SIO_000300 ?score3 .
}
ORDER BY DESC(?score1) DESC(?score2) DESC(?score3)');

insert into info.idsm_queries values (101, 'How can I retrieve all Rhea reactions that involve L-glutamate(1-) (CID 5460299)?', 
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

insert into info.idsm_queries values (102, 'How can I retrieve all compounds involved in the given Rhea reaction (RHEA:10020)?', 
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

insert into info.idsm_queries values (103, 'How can I retrieve the WURCS sequence from Glycosmos for the glycan structure (SID 252275760) in PubChem?', 
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

insert into info.idsm_queries values (104, 'Which PubChem pathways include the genes associated with Keshan disease (DOID:0050083), as identified by Glycosmos?', 
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
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

insert into info.idsm_queries values (105, 'Which PDB structures with a resolution better than 2 Å that include Aspirin (CID 2244) and have associated bioactivity data in PubChem?', 
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX compound: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX bao: <http://www.bioassayontology.org/bao#>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX sio: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?structure ?resolution
WHERE {
  GRAPH pubchem:substance {
    ?substance cheminf:CHEMINF_000477 compound:CID2244 ;
               pdbo:link_to_pdb ?structure .
  }

  SERVICE <https://sparql.uniprot.org/sparql/> {
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

insert into info.idsm_queries values (106, 'How can I retrieve all compounds involved in PDB structures with a resolution better than 2 Å for the protein Basic phospholipase A2 VRV-PL-VIIIa (UniProt ID: P59071)?', 
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX up: <http://purl.uniprot.org/core/>
PREFIX pdbo: <http://rdf.wwpdb.org/schema/pdbx-v50.owl#>
PREFIX cheminf: <http://semanticscience.org/resource/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT ?compound
WHERE {
  SERVICE <https://sparql.uniprot.org/sparql/> {
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

insert into info.idsm_queries values (107, 'How to retrieve the labels of Aspirin (CID 2244) in English and Spanish from Wikidata?', 
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

insert into info.idsm_queries values (108, 'How to retrieve the preferred label from PubChem for the Wikidata entry (Q18216)?', 
'PREFIX wd: <http://www.wikidata.org/entity/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
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

insert into info.idsm_queries values (109, 'How can I find the WikiPathways that include the compound dihydroflavine-adenine dinucleotide (CID 446013)?', 
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
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

    BIND (IRI(REPLACE(STR(?wp), "http://identifiers.org/wikipathways:", "https://identifiers.org/wikipathways/")) AS ?wikipathways)

    SERVICE <https://sparql.wikipathways.org/sparql> {
      ?wikipathways a wp:Pathway ;
                    dcterms:identifier ?wpid .
    }
}');

insert into info.idsm_queries values (110, 'How can I retrieve the CID of compounds involved in the pathway Electron Transport Chain: OXPHOS system in mitochondria (Wikipathways:WP111)?', 
'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX wp: <http://vocabularies.wikipathways.org/wp#>
PREFIX bp3: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/>

SELECT DISTINCT (STRAFTER(STR(?cmpd), "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID") AS ?cid)
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
      FILTER (STRSTARTS(STR(?cmpd), "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/"))
    }
}');

