create table ontology_resource_classes
(
    class_unit    smallint not null,
    class_id      integer not null,
    primary key(class_unit, class_id)
);


create table ontology_resource_properties
(
    property_unit    smallint not null,
    property_id      integer not null,
    primary key(property_unit, property_id)
);


create table ontology_resource_individuals
(
    individual_unit    smallint not null,
    individual_id      integer not null,
    primary key(individual_unit, individual_id)
);


create table ontology_resource_labels
(
    resource_unit    smallint not null,
    resource_id      integer not null,
    label            varchar not null,
    primary key(resource_unit, resource_id)
);


create table ontology_resource_superclasses
(
    class_unit         smallint not null,
    class_id           integer not null,
    superclass_unit    smallint not null,
    superclass_id      integer not null,
    primary key(class_unit, class_id, superclass_unit, superclass_id)
);


create table ontology_resource_superproperties
(
    property_unit         smallint not null,
    property_id           integer not null,
    superproperty_unit    smallint not null,
    superproperty_id      integer not null,
    primary key(property_unit, property_id, superproperty_unit, superproperty_id)
);


create table ontology_resource_domains
(
    property_unit    smallint not null,
    property_id      integer not null,
    domain_unit      smallint not null,
    domain_id        integer not null,
    primary key(property_unit, property_id, domain_unit, domain_id)
);


create table ontology_resource_ranges
(
    property_unit    smallint not null,
    property_id      integer not null,
    range_unit       smallint not null,
    range_id         integer not null,
    primary key(property_unit, property_id, range_unit, range_id)
);


create table ontology_resource_somevaluesfrom_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    class_unit        smallint not null,
    class_id          integer not null,
    primary key(restriction_id)
);


create table ontology_resource_allvaluesfrom_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    class_unit        smallint not null,
    class_id          integer not null,
    primary key(restriction_id)
);


create table ontology_resource_cardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology_resource_mincardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology_resource_maxcardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology_resources__reftable
(
    resource_id      integer not null,
    iri              varchar not null,
    unique(iri),
    primary key(resource_id)
);


create table ontology_resource_categories__reftable
(
    unit_id          smallint not null,
    prefix           varchar not null,
    value_offset     integer not null,
    value_length     integer not null,
    pattern          varchar not null,
    primary key(unit_id)
);

--============================================================================--

insert into ontology_resources__reftable(resource_id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active');
insert into ontology_resources__reftable(resource_id, iri) values (1, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive');
insert into ontology_resources__reftable(resource_id, iri) values (2, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive');
insert into ontology_resources__reftable(resource_id, iri) values (3, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified');
insert into ontology_resources__reftable(resource_id, iri) values (4, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe');
insert into ontology_resources__reftable(resource_id, iri) values (5, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of');
insert into ontology_resources__reftable(resource_id, iri) values (6, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent');
insert into ontology_resources__reftable(resource_id, iri) values (7, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs');
insert into ontology_resources__reftable(resource_id, iri) values (8, 'http://purl.org/spar/fabio/ReviewArticle');
insert into ontology_resources__reftable(resource_id, iri) values (9, 'http://purl.org/spar/fabio/JournalArticle');
insert into ontology_resources__reftable(resource_id, iri) values (10, 'http://www.biopax.org/release/biopax-level3.owl#SmallMolecule');

--============================================================================--

insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (1, 'http://blank/ID_' , 17,  0, '^http://blank/ID_[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (2, 'http://semanticscience.org/resource/SIO_' , 41,  6, '^http://semanticscience.org/resource/SIO_[0-9]{6}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (3, 'http://semanticscience.org/resource/CHEMINF_' , 45,  6, '^http://semanticscience.org/resource/CHEMINF_[0-9]{6}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (4, 'http://www.bioassayontology.org/bao#BAO_' , 41,  7, '^http://www.bioassayontology.org/bao#BAO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (5, 'http://purl.obolibrary.org/obo/GO_' , 35,  7, '^http://purl.obolibrary.org/obo/GO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (6, 'http://purl.obolibrary.org/obo/PR_' , 35,  9, '^http://purl.obolibrary.org/obo/PR_[0-9]{9}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (7, 'http://purl.obolibrary.org/obo/CHEBI_' , 38,  0, '^http://purl.obolibrary.org/obo/CHEBI_[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (8, 'http://purl.bioontology.org/ontology/SNOMEDCT/' , 47,  0, '^http://purl.bioontology.org/ontology/SNOMEDCT/[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (9, 'http://purl.bioontology.org/ontology/NDFRT/N' , 45,  8, '^http://purl.bioontology.org/ontology/NDFRT/N[0-9]{10}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (10, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C' , 53,  0, '^http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (11, 'http://identifiers.org/taxonomy/' , 33,  0, '^http://identifiers.org/taxonomy/[1-9][0-9]*$');

insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (32, 'http://purl.obolibrary.org/obo/PR_' , 35, -1, '^http://purl.obolibrary.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]-(([1-2][0-9])|[1-9])$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (33, 'http://purl.obolibrary.org/obo/PR_' , 35, -1, '^http://purl.obolibrary.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (34, 'https://www.araport.org/locus/AT' , 33, -1, '^https://www.araport.org/locus/AT[A-Z0-9]G[0-9]{5}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (35, 'http://zfin.org/action/marker/view/ZDB-GENE-' , 45, -1, '^http://zfin.org/action/marker/view/ZDB-GENE-[0-9]{6}-(([1-3][0-9]{3})|([1-9][0-9]{0,2}))$');

insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (64, 'http://purl.obolibrary.org/obo/NCBITaxon_' , 42,  0, '^http://purl.obolibrary.org/obo/NCBITaxon_[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (65, 'http://purl.obolibrary.org/obo/UBERON_' , 39,  7, '^http://purl.obolibrary.org/obo/UBERON_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (66, 'http://purl.obolibrary.org/obo/HP_' , 35,  7, '^http://purl.obolibrary.org/obo/HP_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (67, 'http://purl.obolibrary.org/obo/DOID_' , 37,  0, '^http://purl.obolibrary.org/obo/DOID_[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (68, 'http://purl.obolibrary.org/obo/DOID_' , 37,  7, '^http://purl.obolibrary.org/obo/DOID_0[0-9]{6}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (69, 'http://purl.obolibrary.org/obo/PATO_' , 37,  7, '^http://purl.obolibrary.org/obo/PATO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (70, 'http://purl.obolibrary.org/obo/SO_' , 35,  7, '^http://purl.obolibrary.org/obo/SO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (71, 'http://purl.obolibrary.org/obo/CL_' , 35,  7, '^http://purl.obolibrary.org/obo/CL_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (72, 'http://purl.obolibrary.org/obo/SYMP_' , 37,  7, '^http://purl.obolibrary.org/obo/SYMP_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (73, 'http://purl.obolibrary.org/obo/RO_' , 35,  7, '^http://purl.obolibrary.org/obo/RO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (74, 'http://purl.obolibrary.org/obo/UO_' , 35,  7, '^http://purl.obolibrary.org/obo/UO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (75, 'http://purl.obolibrary.org/obo/IAO_' , 36,  7, '^http://purl.obolibrary.org/obo/IAO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (76, 'http://purl.obolibrary.org/obo/OMIM_' , 37,  6, '^http://purl.obolibrary.org/obo/OMIM_[0-9]{6}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (77, 'http://purl.obolibrary.org/obo/CLO_' , 36,  7, '^http://purl.obolibrary.org/obo/CLO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (78, 'http://purl.obolibrary.org/obo/BFO_' , 36,  7, '^http://purl.obolibrary.org/obo/BFO_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (79, 'http://purl.obolibrary.org/obo/TRANS_' , 38,  7, '^http://purl.obolibrary.org/obo/TRANS_[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (80, 'http://purl.obolibrary.org/obo/dictyBase#_DDB_G' , 48,  7, '^http://purl.obolibrary.org/obo/dictyBase#_DDB_G[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (81, 'http://purl.obolibrary.org/obo/EnsemblBacteria#_SAOUHSC_', 57, 5, '^http://purl.obolibrary.org/obo/EnsemblBacteria#_SAOUHSC_[0-9]{5}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (82, 'http://purl.obolibrary.org/obo/MOD_' , 36,  5, '^http://purl.obolibrary.org/obo/MOD_[0-9]{5}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (83, 'http://www.genenames.org/cgi-bin/gene_symbol_report?hgnc_id=', 61, 0, '^http://www.genenames.org/cgi-bin/gene_symbol_report\?hgnc_id=[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (84, 'http://www.informatics.jax.org/marker/MGI:' , 43, 0, '^http://www.informatics.jax.org/marker/MGI:[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (85, 'http://rgd.mcw.edu/rgdweb/report/gene/main.html?id=' , 52, 0, '^http://rgd.mcw.edu/rgdweb/report/gene/main.html\?id=[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (86, 'http://www.yeastgenome.org/cgi-bin/locus.fpl?dbid=S' , 52,  9, '^http://www.yeastgenome.org/cgi-bin/locus.fpl\?dbid=S[0-9]{9}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (87, 'http://www.ncbi.nlm.nih.gov/gene/' , 34,  0, '^http://www.ncbi.nlm.nih.gov/gene/[1-9][0-9]*$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (88, 'http://www.wormbase.org/species/c_elegans/gene/WBGene' , 54,  8, '^http://www.wormbase.org/species/c_elegans/gene/WBGene[0-9]{8}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (89, 'http://www.ecogene.org/gene/EG' , 31,  5, '^http://www.ecogene.org/gene/EG[0-9]{5}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (90, 'http://flybase.org/reports/FBgn' , 32,  7, '^http://flybase.org/reports/FBgn[0-9]{7}$');
insert into ontology_resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (91, 'http://birdgenenames.org/cgnc/GeneReport?id=' , 45,  0, '^http://birdgenenames.org/cgnc/GeneReport\?id=[1-9][0-9]*$');
