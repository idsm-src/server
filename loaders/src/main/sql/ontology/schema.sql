create table ontology.classes
(
    class_unit    smallint not null,
    class_id      integer not null,
    primary key(class_unit, class_id)
);


create table ontology.properties
(
    property_unit    smallint not null,
    property_id      integer not null,
    primary key(property_unit, property_id)
);


create table ontology.individuals
(
    individual_unit    smallint not null,
    individual_id      integer not null,
    primary key(individual_unit, individual_id)
);


create table ontology.resource_labels
(
    resource_unit    smallint not null,
    resource_id      integer not null,
    label            varchar not null,
    primary key(resource_unit, resource_id)
);


create table ontology.superclasses
(
    class_unit         smallint not null,
    class_id           integer not null,
    superclass_unit    smallint not null,
    superclass_id      integer not null,
    primary key(class_unit, class_id, superclass_unit, superclass_id)
);


create table ontology.superproperties
(
    property_unit         smallint not null,
    property_id           integer not null,
    superproperty_unit    smallint not null,
    superproperty_id      integer not null,
    primary key(property_unit, property_id, superproperty_unit, superproperty_id)
);


create table ontology.property_domains
(
    property_unit    smallint not null,
    property_id      integer not null,
    domain_unit      smallint not null,
    domain_id        integer not null,
    primary key(property_unit, property_id, domain_unit, domain_id)
);


create table ontology.property_ranges
(
    property_unit    smallint not null,
    property_id      integer not null,
    range_unit       smallint not null,
    range_id         integer not null,
    primary key(property_unit, property_id, range_unit, range_id)
);


create table ontology.somevaluesfrom_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    class_unit        smallint not null,
    class_id          integer not null,
    primary key(restriction_id)
);


create table ontology.allvaluesfrom_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    class_unit        smallint not null,
    class_id          integer not null,
    primary key(restriction_id)
);


create table ontology.cardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology.mincardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology.maxcardinality_restrictions
(
    restriction_id    integer not null,
    property_unit     smallint not null,
    property_id       integer not null,
    cardinality       integer not null,
    primary key(restriction_id)
);


create table ontology.resources__reftable
(
    resource_id      integer unique not null,
    iri              varchar not null,
    primary key(iri)
);


create table ontology.resource_categories__reftable
(
    unit_id          smallint not null,
    prefix           varchar not null,
    value_offset     integer not null,
    value_length     integer not null,
    pattern          varchar not null,
    primary key(unit_id)
);

--============================================================================--

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (1, 'http://blank/ID_' , 17,  0, '^http://blank/ID_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (2, 'http://semanticscience.org/resource/SIO_' , 41,  6, '^http://semanticscience\.org/resource/SIO_[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (3, 'http://semanticscience.org/resource/CHEMINF_' , 45,  6, '^http://semanticscience\.org/resource/CHEMINF_[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (4, 'http://www.bioassayontology.org/bao#BAO_' , 41,  7, '^http://www\.bioassayontology\.org/bao#BAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (5, 'http://purl.obolibrary.org/obo/GO_' , 35,  7, '^http://purl\.obolibrary\.org/obo/GO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (6, 'http://purl.obolibrary.org/obo/PR_' , 35,  9, '^http://purl\.obolibrary\.org/obo/PR_[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (7, 'http://purl.obolibrary.org/obo/CHEBI_' , 38,  0, '^http://purl\.obolibrary\.org/obo/CHEBI_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (8, 'http://purl.bioontology.org/ontology/SNOMEDCT/' , 47,  0, '^http://purl\.bioontology\.org/ontology/SNOMEDCT/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (9, 'http://purl.bioontology.org/ontology/NDFRT/N' , 45,  8, '^http://purl\.bioontology\.org/ontology/NDFRT/N[0-9]{10}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (10, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C' , 53,  0, '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#C[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (11, 'http://identifiers.org/taxonomy/' , 33,  0, '^http://identifiers\.org/taxonomy/([1-9][0-9]*|0)$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (12, 'http://purl.obolibrary.org/obo/CHEMONTID_' , 42,  7, '^http://purl\.obolibrary\.org/obo/CHEMONTID_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (31, 'http://purl.obolibrary.org/obo/PR_A0A' , 38, -1, '^http://purl\.obolibrary\.org/obo/PR_A0A[0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (32, 'http://purl.obolibrary.org/obo/PR_' , 35, -1, '^http://purl\.obolibrary\.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]-(([1-2][0-9])|[1-9])$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (33, 'http://purl.obolibrary.org/obo/PR_' , 35, -1, '^http://purl\.obolibrary\.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (34, 'https://www.araport.org/locus/AT' , 33, -1, '^https://www\.araport\.org/locus/AT[A-Z0-9]G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (35, 'http://zfin.org/action/marker/view/ZDB-GENE-' , 45, -1, '^http://zfin\.org/action/marker/view/ZDB-GENE-[0-9]{6}-(([1-3][0-9]{3})|([1-9][0-9]{0,2}))$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (64, 'http://purl.obolibrary.org/obo/NCBITaxon_' , 42,  0, '^http://purl\.obolibrary\.org/obo/NCBITaxon_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (65, 'http://purl.obolibrary.org/obo/UBERON_' , 39,  7, '^http://purl\.obolibrary\.org/obo/UBERON_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (66, 'http://purl.obolibrary.org/obo/HP_' , 35,  7, '^http://purl\.obolibrary\.org/obo/HP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (67, 'http://purl.obolibrary.org/obo/DOID_' , 37,  0, '^http://purl\.obolibrary\.org/obo/DOID_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (68, 'http://purl.obolibrary.org/obo/DOID_' , 37,  7, '^http://purl\.obolibrary\.org/obo/DOID_0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (69, 'http://purl.obolibrary.org/obo/PATO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/PATO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (70, 'http://purl.obolibrary.org/obo/SO_' , 35,  7, '^http://purl\.obolibrary\.org/obo/SO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (71, 'http://purl.obolibrary.org/obo/CL_' , 35,  7, '^http://purl\.obolibrary\.org/obo/CL_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (72, 'http://purl.obolibrary.org/obo/SYMP_' , 37,  7, '^http://purl\.obolibrary\.org/obo/SYMP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (73, 'http://purl.obolibrary.org/obo/RO_' , 35,  7, '^http://purl\.obolibrary\.org/obo/RO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (74, 'http://purl.obolibrary.org/obo/UO_' , 35,  7, '^http://purl\.obolibrary\.org/obo/UO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (75, 'http://purl.obolibrary.org/obo/IAO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/IAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (76, 'http://purl.obolibrary.org/obo/OMIM_' , 37,  6, '^http://purl\.obolibrary\.org/obo/OMIM_[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (77, 'http://purl.obolibrary.org/obo/CLO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/CLO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (78, 'http://purl.obolibrary.org/obo/BFO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/BFO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (79, 'http://purl.obolibrary.org/obo/TRANS_' , 38,  7, '^http://purl\.obolibrary\.org/obo/TRANS_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (80, 'http://purl.obolibrary.org/obo/dictyBase#_DDB_G' , 48,  7, '^http://purl\.obolibrary\.org/obo/dictyBase#_DDB_G[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (81, 'http://purl.obolibrary.org/obo/EnsemblBacteria#_SAOUHSC_', 57, 5, '^http://purl\.obolibrary\.org/obo/EnsemblBacteria#_SAOUHSC_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (82, 'http://purl.obolibrary.org/obo/MOD_' , 36,  5, '^http://purl\.obolibrary\.org/obo/MOD_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (83, 'http://www.genenames.org/cgi-bin/gene_symbol_report?hgnc_id=', 61, 0, '^http://www\.genenames\.org/cgi-bin/gene_symbol_report\?hgnc_id=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (84, 'http://www.informatics.jax.org/marker/MGI:' , 43, 0, '^http://www\.informatics\.jax\.org/marker/MGI:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (85, 'http://rgd.mcw.edu/rgdweb/report/gene/main.html?id=' , 52, 0, '^http://rgd\.mcw\.edu/rgdweb/report/gene/main\.html\?id=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (86, 'http://www.yeastgenome.org/cgi-bin/locus.fpl?dbid=S' , 52,  9, '^http://www\.yeastgenome\.org/cgi-bin/locus\.fpl\?dbid=S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (87, 'http://www.ncbi.nlm.nih.gov/gene/' , 34,  0, '^http://www\.ncbi\.nlm\.nih\.gov/gene/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (88, 'http://www.wormbase.org/species/c_elegans/gene/WBGene' , 54,  8, '^http://www\.wormbase\.org/species/c_elegans/gene/WBGene[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (89, 'http://www.ecogene.org/gene/EG' , 31,  5, '^http://www\.ecogene\.org/gene/EG[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (90, 'http://flybase.org/reports/FBgn' , 32,  7, '^http://flybase\.org/reports/FBgn[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (91, 'http://birdgenenames.org/cgnc/GeneReport?id=' , 45,  0, '^http://birdgenenames\.org/cgnc/GeneReport\?id=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (92, 'http://www.ebi.ac.uk/efo/EFO_' , 30,  7, '^http://www\.ebi\.ac\.uk/efo/EFO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (93, 'http://www.orpha.net/consor/cgi-bin/OC_Exp.php?Expert=' , 55,  0, '^http://www\.orpha\.net/consor/cgi-bin/OC_Exp\.php\?Expert=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (94, 'http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MP&termId=MP:' , 69,  7, '^http://www\.ebi\.ac\.uk/ontology-lookup/browse\.do\?ontName=MP&termId=MP:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (95, 'http://purl.obolibrary.org/obo/chebi#' , 38,  0, '^http://purl\.obolibrary\.org/obo/chebi#[1-3]_STAR$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (96, 'http://www.ebi.ac.uk/efo/GO:' , 29,  7, '^http://www\.ebi\.ac\.uk/efo/GO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (97, 'http://www.ebi.ac.uk/efo/DOID:' , 31,  0, '^http://www\.ebi\.ac\.uk/efo/DOID:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (98, 'http://www.ebi.ac.uk/efo/MONDO:' , 32,  7, '^http://www\.ebi\.ac\.uk/efo/MONDO:[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (99, 'http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#N', 48, 10, '^http://evs\.nci\.nih\.gov/ftp1/NDF-RT/NDF-RT\.owl#N[0-9]{10}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (100, 'http://purl.obolibrary.org/obo/NCIT_C', 38, 0, '^http://purl\.obolibrary\.org/obo/NCIT_C[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (101, 'http://purl.obolibrary.org/obo/FOODON_', 39, 8, '^http://purl\.obolibrary\.org/obo/FOODON_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (102, 'http://purl.obolibrary.org/obo/OBI_', 36, 7, '^http://purl\.obolibrary\.org/obo/OBI_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (103, 'http://purl.obolibrary.org/obo/MONDO_', 38, 7, '^http://purl\.obolibrary\.org/obo/MONDO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (104, 'http://purl.obolibrary.org/obo/ERO_', 36, 7, '^http://purl\.obolibrary\.org/obo/ERO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (105, 'http://purl.obolibrary.org/obo/ECO_', 36, 7, '^http://purl\.obolibrary\.org/obo/ECO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (106, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT1G', 61, 5, '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT1G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (107, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT2G', 61, 5, '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT2G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (108, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT3G', 61, 5, '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT3G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (109, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT4G', 61, 5, '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT4G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (110, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT5G', 61, 5, '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT5G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (111, 'http://www.orpha.net/ORDO/Orphanet_', 36, 0, '^http://www\.orpha\.net/ORDO/Orphanet_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (112, 'http://www.yeastgenome.org/locus/S', 35, 9, '^http://www\.yeastgenome\.org/locus/S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (113, 'http://dictybase.org/gene/DDB_G', 32, 7, '^http://dictybase\.org/gene/DDB_G[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (114, 'http://purl.obolibrary.org/obo/ZP_', 35, 7, '^http://purl\.obolibrary\.org/obo/ZP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (115, 'http://purl.obolibrary.org/obo/XPO_', 36, 7, '^http://purl\.obolibrary\.org/obo/XPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (116, 'http://purl.obolibrary.org/obo/UPHENO_', 39, 7, '^http://purl\.obolibrary\.org/obo/UPHENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (117, 'http://purl.obolibrary.org/obo/MP_', 35, 7, '^http://purl\.obolibrary\.org/obo/MP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (118, 'http://purl.obolibrary.org/obo/FYPO_', 37, 7, '^http://purl\.obolibrary\.org/obo/FYPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (119, 'http://purl.obolibrary.org/obo/OBA_', 36, 7, '^http://purl\.obolibrary\.org/obo/OBA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (120, 'http://purl.obolibrary.org/obo/ENVO_', 37, 8, '^http://purl\.obolibrary\.org/obo/ENVO_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (121, 'http://purl.obolibrary.org/obo/ZFA_', 36, 7, '^http://purl\.obolibrary\.org/obo/ZFA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (122, 'http://purl.obolibrary.org/obo/WBPhenotype_', 44, 7, '^http://purl\.obolibrary\.org/obo/WBPhenotype_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (123, 'http://purl.obolibrary.org/obo/OBA_VT', 38, 7, '^http://purl\.obolibrary\.org/obo/OBA_VT[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (124, 'http://purl.obolibrary.org/obo/XAO_', 36, 7, '^http://purl\.obolibrary\.org/obo/XAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (125, 'http://purl.obolibrary.org/obo/DDPHENO_', 40, 7, '^http://purl\.obolibrary\.org/obo/DDPHENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (126, 'http://purl.obolibrary.org/obo/PHIPO_', 38, 7, '^http://purl\.obolibrary\.org/obo/PHIPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (127, 'http://purl.obolibrary.org/obo/CDNO_', 37, 7, '^http://purl\.obolibrary\.org/obo/CDNO_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (128, 'http://purl.obolibrary.org/obo/BTO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/BTO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (129, 'http://purl.obolibrary.org/obo/' , 32,  7, '^http://purl\.obolibrary\.org/obo/[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (130, 'http://purl.obolibrary.org/obo/APO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/APO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (131, 'http://purl.obolibrary.org/obo/BS_' , 35,  5, '^http://purl\.obolibrary\.org/obo/BS_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (132, 'http://purl.obolibrary.org/obo/BSPO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/BSPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (133, 'http://purl.obolibrary.org/obo/BTO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/BTO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (134, 'http://purl.obolibrary.org/obo/CARO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/CARO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (135, 'http://purl.obolibrary.org/obo/DDANAT_' , 39,  7, '^http://purl\.obolibrary\.org/obo/DDANAT_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (136, 'http://purl.obolibrary.org/obo/ENVO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/ENVO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (137, 'http://purl.obolibrary.org/obo/FAO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/FAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (138, 'http://purl.obolibrary.org/obo/FBbt_' , 37,  8, '^http://purl\.obolibrary\.org/obo/FBbt_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (139, 'http://purl.obolibrary.org/obo/FBcv_' , 37,  7, '^http://purl\.obolibrary\.org/obo/FBcv_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (140, 'http://purl.obolibrary.org/obo/FBdv_' , 37,  8, '^http://purl\.obolibrary\.org/obo/FBdv_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (141, 'http://purl.obolibrary.org/obo/GAZ_' , 36,  8, '^http://purl\.obolibrary\.org/obo/GAZ_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (142, 'http://purl.obolibrary.org/obo/GENO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/GENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (143, 'http://purl.obolibrary.org/obo/HANCESTRO_' , 42,  4, '^http://purl\.obolibrary\.org/obo/HANCESTRO_[0-9]{4}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (144, 'http://purl.obolibrary.org/obo/MAXO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/MAXO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (145, 'http://purl.obolibrary.org/obo/MGPO_' , 37,  7, '^http://purl\.obolibrary\.org/obo/MGPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (146, 'http://purl.obolibrary.org/obo/NBO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/NBO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (147, 'http://purl.obolibrary.org/obo/OGMS_' , 37,  7, '^http://purl\.obolibrary\.org/obo/OGMS_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (148, 'http://purl.obolibrary.org/obo/PLANA_' , 38,  7, '^http://purl\.obolibrary\.org/obo/PLANA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (149, 'http://purl.obolibrary.org/obo/PLANP_' , 38,  7, '^http://purl\.obolibrary\.org/obo/PLANP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (150, 'http://purl.obolibrary.org/obo/PO_' , 35,  7, '^http://purl\.obolibrary\.org/obo/PO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (151, 'http://purl.obolibrary.org/obo/RO_HOM' , 38,  7, '^http://purl\.obolibrary\.org/obo/RO_HOM[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (152, 'http://purl.obolibrary.org/obo/WBbt_' , 37,  7, '^http://purl\.obolibrary\.org/obo/WBbt_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (153, 'http://purl.obolibrary.org/obo/WBls_' , 37,  7, '^http://purl\.obolibrary\.org/obo/WBls_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (154, 'http://purl.obolibrary.org/obo/YPO_' , 36,  7, '^http://purl\.obolibrary\.org/obo/YPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (155, 'http://purl.obolibrary.org/obo/MPATH_' , 38,  0, '^http://purl\.obolibrary\.org/obo/MPATH_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (156, 'http://purl.obolibrary.org/obo/NCIT_P' , 38,  0, '^http://purl\.obolibrary\.org/obo/NCIT_P[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (157, 'http://purl.obolibrary.org/obo/NCIT_R' , 38,  0, '^http://purl\.obolibrary\.org/obo/NCIT_R[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (158, 'http://purl.obolibrary.org/obo/ONS_' , 36,  0, '^http://purl\.obolibrary\.org/obo/ONS_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (159, 'http://www.ebi.ac.uk/efo/swo/SWO_' , 34,  7, '^http://www\.ebi\.ac\.uk/efo/swo/SWO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (160, 'https://omim.org/entry/' , 24,  6, '^https://omim\.org/entry/[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (161, 'https://uts.nlm.nih.gov/uts/umls/concept/CN' , 44,  6, '^https://uts\.nlm\.nih\.gov/uts/umls/concept/CN[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (162, 'https://uts.nlm.nih.gov/uts/umls/concept/C' , 43,  7, '^https://uts\.nlm\.nih\.gov/uts/umls/concept/C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (163, 'https://www.ebi.ac.uk/efo/EFO_' , 31,  7, '^https://www\.ebi\.ac\.uk/efo/EFO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (164, 'https://www.ncbi.nlm.nih.gov/medgen/CN' , 39,  6, '^https://www\.ncbi\.nlm\.nih\.gov/medgen/CN[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (165, 'https://www.ncbi.nlm.nih.gov/medgen/C' , 38,  7, '^https://www\.ncbi\.nlm\.nih\.gov/medgen/C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (166, 'http://www.ensembl.org/id/b' , 28,  0, '^http://www\.ensembl\.org/id/b[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (167, 'http://zfin.org/action/marker/view/ZDB-GENE-030131-' , 52,  0, '^http://zfin\.org/action/marker/view/ZDB-GENE-030131-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (168, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R' , 53,  0, '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#R[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (169, 'http://www.ebi.ac.uk/swo/data/SWO_' , 35,  0, '^http://www\.ebi\.ac\.uk/swo/data/SWO_[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (170, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P' , 53,  0, '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#P[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (171, 'http://www.ensemblgenomes.org/id/BMEI' , 38,  0, '^http://www\.ensemblgenomes\.org/id/BMEI[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (172, 'http://lincsportal.ccs.miami.edu/cells/#/view/ES-' , 50,  0, '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/ES-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (173, 'http://lincsportal.ccs.miami.edu/cells/#/view/LCL-' , 51,  0, '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LCL-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (174, 'http://lincsportal.ccs.miami.edu/cells/#/view/LPC-' , 51,  0, '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LPC-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (175, 'http://lincsportal.ccs.miami.edu/cells/#/view/LSC-' , 51,  0, '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LSC-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (176, 'https://lincs.hms.harvard.edu/db/cells/' , 40,  0, '^https://lincs\.hms\.harvard\.edu/db/cells/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (177, 'https://www.ebi.ac.uk/chembl/cell_line_report_card/CHEMBL' , 58,  0, '^https://www\.ebi\.ac\.uk/chembl/cell_line_report_card/CHEMBL[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (178, 'https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=' , 74,  0, '^https://www\.guidetopharmacology\.org/GRAC/DiseaseDisplayForward\?diseaseId=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (179, 'https://www.orpha.net/consor/cgi-bin/OC_Exp.php?Expert=' , 56,  0, '^https://www\.orpha\.net/consor/cgi-bin/OC_Exp\.php\?Expert=[1-9][0-9]*$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (180, 'https://rarediseases.info.nih.gov/diseases/' , 44,  0, '^https://rarediseases\.info\.nih\.gov/diseases/[1-9][0-9]*/index$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (181, 'http://nanbyodata.jp/ontology/NANDO_', 37, 7, '^http://nanbyodata\.jp/ontology/NANDO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (182, 'https://www.kegg.jp/entry/H', 28, 5, '^https://www\.kegg\.jp/entry/H[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (183, 'http://www.wikidata.org/prop/direct/P', 38, 0, '^http://www\.wikidata\.org/prop/direct/P[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (184, 'http://purl.obolibrary.org/obo/MS_', 35, 7, '^http://purl\.obolibrary\.org/obo/MS_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (185, 'http://flybase.org/reports/FBgn', 32, 7, '^http://flybase\.org/reports/FBgn[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (186, 'http://purl.obolibrary.org/obo/DOID:', 37, 0, '^http://purl\.obolibrary\.org/obo/DOID:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (187, 'http://purl.obolibrary.org/obo/DOID:', 37, 7, '^http://purl\.obolibrary\.org/obo/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (188, 'http://purl.uniprot.org/taxonomy/', 34, 0, '^http://purl\.uniprot\.org/taxonomy/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (189, 'https://cancer.sanger.ac.uk/cell_lines/sample/overview?id=', 59, 0, '^https://cancer\.sanger\.ac\.uk/cell_lines/sample/overview\?id=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (190, 'https://cellmodelpassports.sanger.ac.uk/passports/SIDM', 55, 5, '^https://cellmodelpassports\.sanger\.ac\.uk/passports/SIDM[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (191, 'https://ctdbase.org/detail.go?type=gene&acc=', 45, 0, '^https://ctdbase\.org/detail\.go\?type=gene&acc=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (192, 'https://depmap.org/portal/cell_line/ACH-', 41, 6, '^https://depmap\.org/portal/cell_line/ACH-[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (193, 'https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId=', 68, 0, '^https://guidetopharmacology\.org/GRAC/ObjectDisplayForward\?objectId=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (194, 'https://hpo.jax.org/app/browse/term/HP:', 40, 7, '^https://hpo\.jax\.org/app/browse/term/HP:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (195, 'http://identifiers.org/BTO:', 28, 7, '^http://identifiers\.org/BTO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (196, 'http://identifiers.org/CL:', 27, 7, '^http://identifiers\.org/CL:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (197, 'http://identifiers.org/CLO:', 28, 7, '^http://identifiers\.org/CLO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (198, 'http://identifiers.org/ctd.gene:', 33, 0, '^http://identifiers\.org/ctd\.gene:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (199, 'http://identifiers.org/DOID:', 29, 0, '^http://identifiers\.org/DOID:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (200, 'http://identifiers.org/DOID:', 29, 7, '^http://identifiers\.org/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (201, 'http://identifiers.org/efo:', 28, 7, '^http://identifiers\.org/efo:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (202, 'http://identifiers.org/hgnc:', 29, 0, '^http://identifiers\.org/hgnc:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (203, 'http://identifiers.org/hpa:ENSG', 32, 11, '^http://identifiers\.org/hpa:ENSG[0-9]{11}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (204, 'http://identifiers.org/chembl:CHEMBL', 37, 0, '^http://identifiers\.org/chembl:CHEMBL[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (205, 'http://identifiers.org/iuphar.receptor:', 40, 0, '^http://identifiers\.org/iuphar\.receptor:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (206, 'http://identifiers.org/kegg.disease:H', 38, 5, '^http://identifiers\.org/kegg\.disease:H[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (207, 'http://identifiers.org/lincs.cell:LCL-', 39, 0, '^http://identifiers\.org/lincs\.cell:LCL-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (208, 'http://identifiers.org/medgen:C', 32, 7, '^http://identifiers\.org/medgen:C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (209, 'http://identifiers.org/medgen:CN', 33, 6, '^http://identifiers\.org/medgen:CN[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (210, 'http://identifiers.org/MGI:', 28, 0, '^http://identifiers\.org/MGI:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (211, 'http://identifiers.org/mim:', 28, 0, '^http://identifiers\.org/mim:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (212, 'http://identifiers.org/NANDO:', 30, 0, '^http://identifiers\.org/NANDO:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (213, 'http://identifiers.org/ncit:C', 30, 0, '^http://identifiers\.org/ncit:C[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (214, 'http://identifiers.org/orphanet:', 33, 0, '^http://identifiers\.org/orphanet:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (215, 'http://identifiers.org/pharmgkb.disease:PA', 43, 0, '^http://identifiers\.org/pharmgkb\.disease:PA[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (216, 'http://identifiers.org/pharmgkb.gene:PA', 40, 0, '^http://identifiers\.org/pharmgkb\.gene:PA[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (217, 'http://identifiers.org/rgd:', 28, 0, '^http://identifiers\.org/rgd:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (218, 'http://identifiers.org/sgd:S', 29, 9, '^http://identifiers\.org/sgd:S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (219, 'http://identifiers.org/umls:C', 30, 7, '^http://identifiers\.org/umls:C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (220, 'http://identifiers.org/xenbase:XB-GENE-', 40, 0, '^http://identifiers\.org/xenbase:XB-GENE-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (221, 'http://identifiers.org/xenbase:XB-GENEPAGE-', 44, 0, '^http://identifiers\.org/xenbase:XB-GENEPAGE-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (222, 'https://monarchinitiative.org/disease/MONDO:', 45, 7, '^https://monarchinitiative\.org/disease/MONDO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (223, 'https://omim.org/entry/', 24, 0, '^https://omim\.org/entry/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (224, 'https://platform.opentargets.org/target/ENSG', 45, 11, '^https://platform\.opentargets\.org/target/ENSG[0-9]{11}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (225, 'https://rgd.mcw.edu/rgdweb/report/gene/main.html?id=', 53, 0, '^https://rgd\.mcw\.edu/rgdweb/report/gene/main\.html\?id=[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (226, 'https://search.thegencc.org/genes/HGNC:', 40, 0, '^https://search\.thegencc\.org/genes/HGNC:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (227, 'https://www.cancerrxgene.org/translation/CellLine/', 51, 0, '^https://www\.cancerrxgene\.org/translation/CellLine/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (228, 'https://www.disease-ontology.org/?id=DOID:', 43, 0, '^https://www\.disease-ontology\.org/\?id=DOID:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (229, 'https://www.disease-ontology.org/?id=DOID:', 43, 7, '^https://www\.disease-ontology\.org/\?id=DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (230, 'https://www.drugbank.ca/bio_entities/BE', 40, 7, '^https://www\.drugbank\.ca/bio_entities/BE[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (231, 'https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL', 55, 0, '^https://www\.ebi\.ac\.uk/chembl/target_report_card/CHEMBL[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (232, 'https://www.ebi.ac.uk/chembl/tissue_report_card/CHEMBL', 55, 0, '^https://www\.ebi\.ac\.uk/chembl/tissue_report_card/CHEMBL[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (233, 'https://www.genenames.org/data/gene-symbol-report/#!/hgnc_id/', 62, 0, '^https://www\.genenames\.org/data/gene-symbol-report/#!/hgnc_id/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (234, 'https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=', 80, 0, '^https://www\.itis\.gov/servlet/SingleRpt/SingleRpt\?search_topic=TSN&search_value=[0-9]+$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (235, 'https://www.nextprot.org/term/TS-', 34, 4, '^https://www\.nextprot\.org/term/TS-[0-9]{4}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (236, 'https://www.orpha.net/en/disease/detail/', 41, 0, '^https://www\.orpha\.net/en/disease/detail/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (237, 'https://www.pharmgkb.org/disease/PA', 36, 0, '^https://www\.pharmgkb\.org/disease/PA[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (238, 'https://www.pharmgkb.org/gene/PA', 33, 0, '^https://www\.pharmgkb\.org/gene/PA[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (239, 'https://www.proteinatlas.org/ENSG', 34, 11, '^https://www\.proteinatlas\.org/ENSG[0-9]{11}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (240, 'https://www.xenbase.org/gene/showgene.do?method=display&geneId=XB-GENE-', 72, 0, '^https://www\.xenbase\.org/gene/showgene\.do\?method=display&geneId=XB-GENE-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (241, 'https://www.xenbase.org/gene/showgene.do?method=display&geneId=XB-GENEPAGE-', 76, 0, '^https://www\.xenbase\.org/gene/showgene\.do\?method=display&geneId=XB-GENEPAGE-[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (242, 'https://www.yeastgenome.org/locus/S', 36, 9, '^https://www\.yeastgenome\.org/locus/S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (243, 'http://www.informatics.jax.org/marker/MGI:', 43, 0, '^http://www\.informatics\.jax\.org/marker/MGI:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (244, 'http://www.wormbase.org/db/gene/gene?name=WBGene', 49, 8, '^http://www\.wormbase\.org/db/gene/gene\?name=WBGene[0-9]{8};class=Gene$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (245, 'http://identifiers.org/HP:', 27, 7, '^http://identifiers\.org/HP:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (246, 'https://glycosmos.org/diseases/DOID:', 37, 0, '^https://glycosmos\.org/diseases/DOID:[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (247, 'https://glycosmos.org/diseases/DOID:', 37, 7, '^https://glycosmos\.org/diseases/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (248, 'https://glycosmos.org/organisms/show/' , 38,  0, '^https://glycosmos\.org/organisms/show/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (249, 'http://glycosmos.org/glycogene/' , 32,  0, '^http://glycosmos\.org/glycogene/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (250, 'http://rdf.glycoinfo.org/source/' , 33,  0, '^http://rdf\.glycoinfo\.org/source/[1-9][0-9]*$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (251, 'http://identifiers.org/UBERON:' , 31,  7, '^http://identifiers\.org/UBERON:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (252, 'http://identifiers.org/fb:FBgn' , 31,  7, '^http://identifiers\.org/fb:FBgn[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, pattern) values (253, 'http://identifiers.org/ncbigene:' , 33,  0, '^http://identifiers\.org/ncbigene:[1-9][0-9]*$');
