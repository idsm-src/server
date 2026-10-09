create table idsm.sources
(
    id          integer not null,
    name        varchar unique not null,
    url         varchar not null,
    version     varchar not null,
    primary key(id)
);


create table idsm.stats
(
    id          integer not null,
    name        varchar unique not null,
    count       integer not null,
    primary key(id)
);


create table idsm.sparql_endpoints
(
    iri         varchar not null,
    primary key(iri)
);


create table idsm.queries
(
    id          integer not null,
    comment     varchar not null,
    target      varchar not null,
    query       varchar not null,
    primary key(id)
);


create table idsm.federated_queries
(
    id          integer not null,
    comment     varchar not null,
    target      varchar not null,
    query       varchar not null,
    primary key(id)
);


create table idsm.federated_query_targets
(
    query       integer not null,
    target      varchar not null,
    primary key(query, target)
);


create table idsm.version
(
    date        timestamptz not null
);

--============================================================================--

insert into idsm.version(date) values (now());


insert into idsm.sources values ( 0, 'PubChemRDF', 'https://pubchemdocs.ncbi.nlm.nih.gov/rdf', '');
insert into idsm.sources values ( 1, 'PubChem BioAssays (XML)', 'https://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/XML/', '');
insert into idsm.sources values ( 2, 'PubChem Compounds (SDF)', 'https://ftp.ncbi.nlm.nih.gov/pubchem/Compound/CURRENT-Full/SDF/', '');
insert into idsm.sources values ( 3, 'ChEMBL', 'https://www.ebi.ac.uk/chembl/', '');
insert into idsm.sources values ( 4, 'ChEBI Ontology', 'https://www.ebi.ac.uk/chebi/', '');
insert into idsm.sources values ( 5, 'PDB Chemical Components (PDBeChem)', 'https://www.ebi.ac.uk/pdbe-srv/pdbechem/', '');
insert into idsm.sources values ( 6, 'Wikidata Compounds', 'https://www.wikidata.org/', '');
insert into idsm.sources values ( 7, 'DrugBank Compounds', 'https://drugbank.com/', '');
insert into idsm.sources values ( 8, 'MassBank of North America (MoNA)', 'https://mona.fiehnlab.ucdavis.edu/', '');
insert into idsm.sources values ( 9, 'In Silico Spectral Database (ISDB)', 'https://zenodo.org/records/14887271', '');
insert into idsm.sources values (10, 'Medical Subject Headings (MESH)', 'https://id.nlm.nih.gov/mesh/', '');
insert into idsm.sources values (11, 'BioAssay Ontology (BAO)', 'http://bioassayontology.org/bioassayontology/', '');
insert into idsm.sources values (12, 'Protein Ontology (PRO)', 'https://proconsortium.org', '');
insert into idsm.sources values (13, 'Gene Ontology (GO)', 'http://geneontology.org', '');
insert into idsm.sources values (14, 'Sequence Ontology (SO)', 'http://www.sequenceontology.org', '');
insert into idsm.sources values (15, 'Cell Line Ontology (CLO)', 'http://www.clo-ontology.org', '');
insert into idsm.sources values (16, 'Cell Ontology (CL)', 'https://obophenotype.github.io/cell-ontology/', '');
insert into idsm.sources values (17, 'The BRENDA Tissue Ontology (BTO)', 'https://www.brenda-enzymes.org', '');
insert into idsm.sources values (18, 'Human Disease Ontology (DO)', 'https://disease-ontology.org', '');
insert into idsm.sources values (19, 'Mondo Disease Ontology (MONDO)', 'http://obofoundry.org/ontology/mondo.html', '');
insert into idsm.sources values (20, 'Symptom Ontology (SYMP)', 'http://symptomontologywiki.igs.umaryland.edu/mediawiki/index.php', '');
insert into idsm.sources values (21, 'Pathogen Transmission Ontology (TRANS)', 'https://github.com/DiseaseOntology/PathogenTransmissionOntology', '');
insert into idsm.sources values (22, 'The Human Phenotype Ontology (HP)', 'http://www.human-phenotype-ontology.org', '');
insert into idsm.sources values (23, 'Phenotype And Trait Ontology (PATO)', 'https://github.com/pato-ontology/pato/', '');
insert into idsm.sources values (24, 'Units of Measurement Ontology (UO)', 'https://github.com/bio-ontology-research-group/unit-ontology', '');
insert into idsm.sources values (25, 'Ontology for Biomedical Investigations (OBI)', 'http://obi-ontology.org', '');
insert into idsm.sources values (26, 'Information Artifact Ontology (IAO)', 'https://github.com/information-artifact-ontology/IAO/', '');
insert into idsm.sources values (27, 'Uber-anatomy Ontology (UBERON)', 'http://obophenotype.github.io/uberon/', '');
insert into idsm.sources values (28, 'NCBI Taxonomy Database', 'https://www.ncbi.nlm.nih.gov/taxonomy', '');
insert into idsm.sources values (29, 'National Center Institute Thesaurus (OBO Edition)', 'https://github.com/NCI-Thesaurus/thesaurus-obo-edition', '');
insert into idsm.sources values (30, 'OBO Relations Ontology', 'https://oborel.github.io', '');
insert into idsm.sources values (31, 'Basic Formal Ontology (BFO)', 'https://basic-formal-ontology.org', '');
insert into idsm.sources values (32, 'Food Ontology (FOODON)', 'http://foodon.org', '');
insert into idsm.sources values (33, 'Evidence and Conclusion Ontology (ECO)', 'http://evidenceontology.org', '');
insert into idsm.sources values (34, 'Disease Drivers Ontology (DISDRIV)', 'http://www.disease-ontology.org', '');
insert into idsm.sources values (35, 'Genotype Ontology (GENO)', 'https://github.com/monarch-initiative/GENO-ontology/', '');
insert into idsm.sources values (36, 'Common Anatomy Reference Ontology (CARO)', 'https://github.com/obophenotype/caro/', '');
insert into idsm.sources values (37, 'Environment Ontology (ENVO)', 'http://environmentontology.org', '');
insert into idsm.sources values (38, 'Ontology for General Medical Science (OGMS)', 'https://github.com/OGMS/ogms', '');
insert into idsm.sources values (39, 'Unified phenotype ontology (uPheno)', 'https://github.com/obophenotype/upheno', '');
insert into idsm.sources values (40, 'OBO Metadata Ontology', 'https://github.com/information-artifact-ontology/ontology-metadata', '');
insert into idsm.sources values (41, 'Biological Pathway Exchange (BioPAX)', 'http://www.biopax.org/', '');
insert into idsm.sources values (42, 'UniProt RDF schema ontology', 'https://www.uniprot.org', '');
insert into idsm.sources values (43, 'PDBx ontology', 'https://pdbj.org/', '');
insert into idsm.sources values (44, 'Quantities, Units, Dimensions and Types Ontology (QUDT)', 'http://qudt.org', '');
insert into idsm.sources values (45, 'Open PHACTS Units extending QUDT', 'http://www.openphacts.org/specs/units/', '');
insert into idsm.sources values (46, 'Shapes Constraint Language (SHACL)', 'https://www.w3.org/TR/shacl/', '');
insert into idsm.sources values (47, 'Linked Models: Datatype Ontology (DTYPE)', 'http://www.linkedmodel.org/', '');
insert into idsm.sources values (48, 'Linked Models: Vocabulary for Attaching Essential Metadata (VAEM)', 'http://www.linkedmodel.org/', '');
insert into idsm.sources values (49, 'Chemical Information Ontology (CHEMINF)', 'http://semanticchemistry.github.io/semanticchemistry/', '');
insert into idsm.sources values (50, 'Semanticscience integrated ontology (SIO)', 'https://sio.semanticscience.org/', '');
insert into idsm.sources values (51, 'Ontology of Bioscientific Data Analysis and Data Management (EDAM)', 'https://edamontology.org/', '');
insert into idsm.sources values (52, 'National Drug File-Reference Terminology (NDF-RT)', 'https://www.oit.va.gov/Services/TRM/StandardPage.aspx?tid=5221', '');
insert into idsm.sources values (53, 'National Center Institute Thesaurus (NCIt)', 'http://ncit.nci.nih.gov/', '');
insert into idsm.sources values (54, 'Experimental Factor Ontology (EFO)', 'https://www.ebi.ac.uk/efo/', '');
insert into idsm.sources values (55, 'Eagle-i Resource Ontology (ERO)', 'https://www.eagle-i.net/', '');
insert into idsm.sources values (56, 'Funding, Research Administration and Projects Ontology (FRAPO)', 'http://purl.org/cerif/frapo', '');
insert into idsm.sources values (57, 'Patent Ontology (EPO)', 'https://data.epo.org/linked-data/', '');
insert into idsm.sources values (58, 'W3C PROVenance Interchange', 'http://www.w3.org/TR/prov-overview/', '');
insert into idsm.sources values (59, 'Metadata Authority Description Schema in RDF (MADS/RDF)', 'http://www.loc.gov/standards/mads/rdf/', '');
insert into idsm.sources values (60, 'Citation Typing Ontology (CiTO)', 'https://sparontologies.github.io/cito/current/cito.html', '');
insert into idsm.sources values (61, 'Ontology for vCard', 'https://www.w3.org/TR/vcard-rdf/', '');
insert into idsm.sources values (62, 'Feature Annotation Location Description Ontology (FALDO)', 'http://biohackathon.org/resource/faldo', '');
insert into idsm.sources values (63, 'FRBR-aligned Bibliographic Ontology (FaBiO)', 'https://sparontologies.github.io/fabio/current/fabio.html', '');
insert into idsm.sources values (64, 'Essential FRBR in OWL2 DL Ontology (FRBR)', 'https://sparontologies.github.io/frbr/current/frbr.html', '');
insert into idsm.sources values (65, 'Dublin Core Metadata Initiative Terms (DCMI)', 'https://dublincore.org/specifications/dublin-core/dcmi-terms/', '');
insert into idsm.sources values (66, 'Bibliographic Ontology (BIBO)', 'https://www.dublincore.org/specifications/bibo/', '');
insert into idsm.sources values (67, 'Simple Knowledge Organization System (SKOS)', 'https://www.w3.org/2009/08/skos-reference/skos.html', '');
insert into idsm.sources values (68, 'Description of a Project Vocabulary (DOAP)', 'https://github.com/ewilderj/doap/wiki', '');
insert into idsm.sources values (69, 'FOAF Vocabulary', 'http://xmlns.com/foaf/0.1/', '');
insert into idsm.sources values (70, 'Provenance, Authoring and Versioning (PAV)', 'http://pav-ontology.github.io/pav/', '');
insert into idsm.sources values (71, 'SemWeb Vocab Status Ontology', 'https://www.w3.org/2003/06/sw-vocab-status/note.html', '');
insert into idsm.sources values (72, 'Vocabulary of Interlinked Datasets (VoID)', 'http://vocab.deri.ie/void.html', '');
insert into idsm.sources values (73, 'Situation Ontology', 'http://ontologydesignpatterns.org/wiki/Submissions:Situation', '');
insert into idsm.sources values (74, 'Mass Spectrometry Ontology (MS)', 'http://www.psidev.info/groups/controlled-vocabularies', '');
insert into idsm.sources values (75, 'ClassyFire Ontology', 'http://classyfire.wishartlab.com/', '');
insert into idsm.sources values (76, 'Chemical Entity Materials and Reactions Ontological Framework (ChEMROF)', 'https://chemkg.github.io/chemrof/home/', '');
insert into idsm.sources values (77, 'OWL 2 Schema (OWL 2)', 'https://www.w3.org/TR/owl2-overview/', '');
insert into idsm.sources values (78, 'RDF Schema (RDFS)', 'https://www.w3.org/TR/rdf-schema/', '');
insert into idsm.sources values (79, 'RDF Vocabulary Terms', 'https://www.w3.org/TR/rdf11-concepts/', '');


insert into idsm.stats values ( 0, 'PubChem Substances', 0);
insert into idsm.stats values ( 1, 'PubChem Compounds', 0);
insert into idsm.stats values ( 2, 'PubChem BioAssays', 0);
insert into idsm.stats values ( 3, 'ChEMBL Substances', 0);
insert into idsm.stats values ( 4, 'ChEMBL Assays', 0);
insert into idsm.stats values ( 5, 'ChEBI Entities', 0);
insert into idsm.stats values ( 6, 'PDB Chemical Components', 0);
insert into idsm.stats values ( 7, 'Wikidata Chemical Entities', 0);
insert into idsm.stats values ( 8, 'DrugBank Compounds', 0);
insert into idsm.stats values ( 9, 'MoNA Mass Spectra', 0);
insert into idsm.stats values (10, 'ISDB Mass Spectra', 0);


insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/chebi');
insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/chembl');
insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/drugbank');
insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/idsm');
insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/pubchem');
insert into idsm.sparql_endpoints values ('https://idsm.elixir-czech.cz/sparql/endpoint/wikidata');
insert into idsm.sparql_endpoints values ('https://query.wikidata.org/sparql');
insert into idsm.sparql_endpoints values ('https://sparql.api.identifiers.org/sparql');
insert into idsm.sparql_endpoints values ('https://sparql.rhea-db.org/sparql');
insert into idsm.sparql_endpoints values ('https://sparql.uniprot.org/sparql');
insert into idsm.sparql_endpoints values ('https://sparql.wikipathways.org/sparql');
insert into idsm.sparql_endpoints values ('https://ts.glycosmos.org/sparql');
