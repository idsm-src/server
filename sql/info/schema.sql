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

insert into info.idsm_sources values (0, 'PubChemRDF', 'https://pubchemdocs.ncbi.nlm.nih.gov/rdf', '');
insert into info.idsm_sources values (1, 'ChEMBL', 'https://www.ebi.ac.uk/chembl/', '');
insert into info.idsm_sources values (2, 'ChEBI Ontology', 'https://www.ebi.ac.uk/chebi/', '');
insert into info.idsm_sources values (3, 'Medical Subject Headings (MESH)', 'https://id.nlm.nih.gov/mesh/', '');
insert into info.idsm_sources values (4, 'BioAssay Ontology (BAO)', 'http://bioassayontology.org/bioassayontology/', '');
insert into info.idsm_sources values (5, 'Protein Ontology (PRO)', 'https://proconsortium.org', '');
insert into info.idsm_sources values (6, 'Gene Ontology (GO)', 'http://geneontology.org', '');
insert into info.idsm_sources values (7, 'Sequence Ontology (SO)', 'http://www.sequenceontology.org', '');
insert into info.idsm_sources values (8, 'Cell Line Ontology (CLO)', 'http://www.clo-ontology.org', '');
insert into info.idsm_sources values (9, 'Cell Ontology (CL)', 'https://obophenotype.github.io/cell-ontology/', '');
insert into info.idsm_sources values (10, 'The BRENDA Tissue Ontology (BTO)', 'https://www.brenda-enzymes.org', '');
insert into info.idsm_sources values (11, 'Human Disease Ontology (DO)', 'https://disease-ontology.org', '');
insert into info.idsm_sources values (12, 'Mondo Disease Ontology (MONDO)', 'http://obofoundry.org/ontology/mondo.html', '');
insert into info.idsm_sources values (13, 'Symptom Ontology (SYMP)', 'http://symptomontologywiki.igs.umaryland.edu/mediawiki/index.php', '');
insert into info.idsm_sources values (14, 'Pathogen Transmission Ontology (TRANS)', 'https://github.com/DiseaseOntology/PathogenTransmissionOntology', '');
insert into info.idsm_sources values (15, 'The Human Phenotype Ontology (HP)', 'http://www.human-phenotype-ontology.org', '');
insert into info.idsm_sources values (16, 'Phenotype And Trait Ontology (PATO)', 'https://github.com/pato-ontology/pato/', '');
insert into info.idsm_sources values (17, 'Units of Measurement Ontology (UO)', 'https://github.com/bio-ontology-research-group/unit-ontology', '');
insert into info.idsm_sources values (18, 'Ontology for Biomedical Investigations (OBI)', 'http://obi-ontology.org', '');
insert into info.idsm_sources values (19, 'Information Artifact Ontology (IAO)', 'https://github.com/information-artifact-ontology/IAO/', '');
insert into info.idsm_sources values (20, 'Uber-anatomy Ontology (UBERON)', 'http://obophenotype.github.io/uberon/', '');
insert into info.idsm_sources values (21, 'NCBI Taxonomy Database', 'https://www.ncbi.nlm.nih.gov/taxonomy', '');
insert into info.idsm_sources values (22, 'National Center Institute Thesaurus (OBO Edition)', 'https://github.com/NCI-Thesaurus/thesaurus-obo-edition', '');
insert into info.idsm_sources values (23, 'OBO Relations Ontology', 'https://oborel.github.io', '');
insert into info.idsm_sources values (24, 'Basic Formal Ontology (BFO)', 'https://basic-formal-ontology.org', '');
insert into info.idsm_sources values (25, 'Food Ontology (FOODON)', 'http://foodon.org', '');
insert into info.idsm_sources values (26, 'Evidence and Conclusion Ontology (ECO)', 'http://evidenceontology.org', '');
insert into info.idsm_sources values (27, 'Disease Drivers Ontology (DISDRIV)', 'http://www.disease-ontology.org', '');
insert into info.idsm_sources values (28, 'Genotype Ontology (GENO)', 'https://github.com/monarch-initiative/GENO-ontology/', '');
insert into info.idsm_sources values (29, 'Common Anatomy Reference Ontology (CARO)', 'https://github.com/obophenotype/caro/', '');
insert into info.idsm_sources values (30, 'Environment Ontology (ENVO)', 'http://environmentontology.org', '');
insert into info.idsm_sources values (31, 'Ontology for General Medical Science (OGMS)', 'https://github.com/OGMS/ogms', '');
insert into info.idsm_sources values (32, 'Unified phenotype ontology (uPheno)', 'https://github.com/obophenotype/upheno', '');
insert into info.idsm_sources values (33, 'OBO Metadata Ontology', 'https://github.com/information-artifact-ontology/ontology-metadata', '');
insert into info.idsm_sources values (34, 'Biological Pathway Exchange (BioPAX)', 'http://www.biopax.org/', '');
insert into info.idsm_sources values (35, 'UniProt RDF schema ontology', 'https://www.uniprot.org', '');
insert into info.idsm_sources values (36, 'PDBx ontology', 'https://pdbj.org/', '');
insert into info.idsm_sources values (37, 'Quantities, Units, Dimensions and Types Ontology (QUDT)', 'http://qudt.org', '');
insert into info.idsm_sources values (38, 'Open PHACTS Units extending QUDT', 'http://www.openphacts.org/specs/units/', '');
insert into info.idsm_sources values (39, 'Shapes Constraint Language (SHACL)', 'https://www.w3.org/TR/shacl/', '');
insert into info.idsm_sources values (40, 'Linked Models: Datatype Ontology (DTYPE)', 'http://www.linkedmodel.org/', '');
insert into info.idsm_sources values (41, 'Linked Models: Vocabulary for Attaching Essential Metadata (VAEM)', 'http://www.linkedmodel.org/', '');
insert into info.idsm_sources values (42, 'Chemical Information Ontology (CHEMINF)', 'http://semanticchemistry.github.io/semanticchemistry/', '');
insert into info.idsm_sources values (43, 'Semanticscience integrated ontology (SIO)', 'https://sio.semanticscience.org/', '');
insert into info.idsm_sources values (44, 'Ontology of Bioscientific Data Analysis and Data Management (EDAM)', 'https://edamontology.org/', '');
insert into info.idsm_sources values (45, 'National Drug File-Reference Terminology (NDF-RT)', 'https://www.oit.va.gov/Services/TRM/StandardPage.aspx?tid=5221', '');
insert into info.idsm_sources values (46, 'National Center Institute Thesaurus (NCIt)', 'http://ncit.nci.nih.gov/', '');
insert into info.idsm_sources values (47, 'Experimental Factor Ontology (EFO)', 'https://www.ebi.ac.uk/efo/', '');
insert into info.idsm_sources values (48, 'Eagle-i Resource Ontology (ERO)', 'https://www.eagle-i.net/', '');
insert into info.idsm_sources values (49, 'Funding, Research Administration and Projects Ontology (FRAPO)', 'http://purl.org/cerif/frapo', '');
insert into info.idsm_sources values (50, 'Patent Ontology (EPO)', 'https://data.epo.org/linked-data/', '');
insert into info.idsm_sources values (51, 'W3C PROVenance Interchange', 'http://www.w3.org/TR/prov-overview/', '');
insert into info.idsm_sources values (52, 'Metadata Authority Description Schema in RDF (MADS/RDF)', 'http://www.loc.gov/standards/mads/rdf/', '');
insert into info.idsm_sources values (53, 'Citation Typing Ontology (CiTO)', 'https://sparontologies.github.io/cito/current/cito.html', '');
insert into info.idsm_sources values (54, 'Ontology for vCard', 'https://www.w3.org/TR/vcard-rdf/', '');
insert into info.idsm_sources values (55, 'Feature Annotation Location Description Ontology (FALDO)', 'http://biohackathon.org/resource/faldo', '');
insert into info.idsm_sources values (56, 'FRBR-aligned Bibliographic Ontology (FaBiO)', 'https://sparontologies.github.io/fabio/current/fabio.html', '');
insert into info.idsm_sources values (57, 'Essential FRBR in OWL2 DL Ontology (FRBR)', 'https://sparontologies.github.io/frbr/current/frbr.html', '');
insert into info.idsm_sources values (58, 'Dublin Core Metadata Initiative Terms (DCMI)', 'https://dublincore.org/specifications/dublin-core/dcmi-terms/', '');
insert into info.idsm_sources values (59, 'Bibliographic Ontology (BIBO)', 'https://www.dublincore.org/specifications/bibo/', '');
insert into info.idsm_sources values (60, 'Simple Knowledge Organization System (SKOS)', 'https://www.w3.org/2009/08/skos-reference/skos.html', '');
insert into info.idsm_sources values (61, 'Description of a Project Vocabulary (DOAP)', 'https://github.com/ewilderj/doap/wiki', '');
insert into info.idsm_sources values (62, 'FOAF Vocabulary', 'http://xmlns.com/foaf/0.1/', '');
insert into info.idsm_sources values (63, 'Provenance, Authoring and Versioning (PAV)', 'http://pav-ontology.github.io/pav/', '');
insert into info.idsm_sources values (64, 'SemWeb Vocab Status Ontology', 'https://www.w3.org/2003/06/sw-vocab-status/note.html', '');
insert into info.idsm_sources values (65, 'Vocabulary of Interlinked Datasets (VoID)', 'http://vocab.deri.ie/void.html', '');
insert into info.idsm_sources values (66, 'Situation Ontology', 'http://ontologydesignpatterns.org/wiki/Submissions:Situation', '');
insert into info.idsm_sources values (67, 'OWL 2 Schema (OWL 2)', 'https://www.w3.org/TR/owl2-overview/', '');
insert into info.idsm_sources values (68, 'RDF Schema (RDFS)', 'https://www.w3.org/TR/rdf-schema/', '');
insert into info.idsm_sources values (69, 'RDF Vocabulary Terms', 'https://www.w3.org/TR/rdf11-concepts/', '');

insert into info.idsm_counts values (0, 'PubChem Substances', 0);
insert into info.idsm_counts values (1, 'PubChem Compounds', 0);
insert into info.idsm_counts values (2, 'PubChem BioAssays', 0);
insert into info.idsm_counts values (3, 'ChEMBL Substances', 0);
insert into info.idsm_counts values (4, 'ChEMBL Assays', 0);
insert into info.idsm_counts values (5, 'ChEBI Entities', 0);
