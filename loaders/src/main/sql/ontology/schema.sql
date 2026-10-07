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
    suffix           varchar not null,
    pattern          varchar not null,
    primary key(unit_id)
);

--============================================================================--

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (   1, 'http://blank/ID_', 17, 0, '', '^http://blank/ID_(0|[1-9][0-9]{0,8})$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 100, 'http://purl.obolibrary.org/obo/PR_', 35, -1, '', '^http://purl\.obolibrary\.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 101, 'http://purl.obolibrary.org/obo/PR_', 35, -1, '', '^http://purl\.obolibrary\.org/obo/PR_[A-Z][0-9][A-Z0-9]{3}[0-9]-(([1-2][0-9])|[1-9])$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 102, 'http://purl.obolibrary.org/obo/PR_A0A', 38, -1, '', '^http://purl\.obolibrary\.org/obo/PR_A0A[0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 200, 'http://purl.obolibrary.org/obo/', 32, 7, '', '^http://purl\.obolibrary\.org/obo/[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 201, 'http://purl.obolibrary.org/obo/APO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/APO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 202, 'http://purl.obolibrary.org/obo/BFO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/BFO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 203, 'http://purl.obolibrary.org/obo/BS_', 35, 5, '', '^http://purl\.obolibrary\.org/obo/BS_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 204, 'http://purl.obolibrary.org/obo/BSPO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/BSPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 205, 'http://purl.obolibrary.org/obo/BTO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/BTO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 206, 'http://purl.obolibrary.org/obo/CARO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/CARO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 207, 'http://purl.obolibrary.org/obo/CDNO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/CDNO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 208, 'http://purl.obolibrary.org/obo/chebi/', 38, 0, '_STAR', '^http://purl\.obolibrary\.org/obo/chebi/[1-3]_STAR$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 209, 'http://purl.obolibrary.org/obo/CHEBI_', 38, 0, '', '^http://purl\.obolibrary\.org/obo/CHEBI_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 210, 'http://purl.obolibrary.org/obo/CHEMONTID_', 42, 7, '', '^http://purl\.obolibrary\.org/obo/CHEMONTID_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 211, 'http://purl.obolibrary.org/obo/CL_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/CL_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 212, 'http://purl.obolibrary.org/obo/CLO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/CLO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 213, 'http://purl.obolibrary.org/obo/DDANAT_', 39, 7, '', '^http://purl\.obolibrary\.org/obo/DDANAT_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 214, 'http://purl.obolibrary.org/obo/DDPHENO_', 40, 7, '', '^http://purl\.obolibrary\.org/obo/DDPHENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 215, 'http://purl.obolibrary.org/obo/dictyBase#_DDB_G', 48, 7, '', '^http://purl\.obolibrary\.org/obo/dictyBase#_DDB_G[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 216, 'http://purl.obolibrary.org/obo/DOID:', 37, 0, '', '^http://purl\.obolibrary\.org/obo/DOID:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 217, 'http://purl.obolibrary.org/obo/DOID:', 37, 7, '', '^http://purl\.obolibrary\.org/obo/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 218, 'http://purl.obolibrary.org/obo/DOID_', 37, 0, '', '^http://purl\.obolibrary\.org/obo/DOID_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 219, 'http://purl.obolibrary.org/obo/DOID_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/DOID_0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 220, 'http://purl.obolibrary.org/obo/ECO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/ECO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 221, 'http://purl.obolibrary.org/obo/EnsemblBacteria#_SAOUHSC_', 57, 5, '', '^http://purl\.obolibrary\.org/obo/EnsemblBacteria#_SAOUHSC_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 222, 'http://purl.obolibrary.org/obo/ENVO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/ENVO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 223, 'http://purl.obolibrary.org/obo/ENVO_', 37, 8, '', '^http://purl\.obolibrary\.org/obo/ENVO_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 224, 'http://purl.obolibrary.org/obo/ERO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/ERO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 225, 'http://purl.obolibrary.org/obo/FAO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/FAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 226, 'http://purl.obolibrary.org/obo/FBbt_', 37, 8, '', '^http://purl\.obolibrary\.org/obo/FBbt_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 227, 'http://purl.obolibrary.org/obo/FBcv_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/FBcv_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 228, 'http://purl.obolibrary.org/obo/FBdv_', 37, 8, '', '^http://purl\.obolibrary\.org/obo/FBdv_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 229, 'http://purl.obolibrary.org/obo/FOODON_', 39, 8, '', '^http://purl\.obolibrary\.org/obo/FOODON_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 230, 'http://purl.obolibrary.org/obo/FYPO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/FYPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 231, 'http://purl.obolibrary.org/obo/GAZ_', 36, 8, '', '^http://purl\.obolibrary\.org/obo/GAZ_[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 232, 'http://purl.obolibrary.org/obo/GENO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/GENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 233, 'http://purl.obolibrary.org/obo/GO_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/GO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 234, 'http://purl.obolibrary.org/obo/HANCESTRO_', 42, 4, '', '^http://purl\.obolibrary\.org/obo/HANCESTRO_[0-9]{4}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 235, 'http://purl.obolibrary.org/obo/HP_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/HP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 236, 'http://purl.obolibrary.org/obo/IAO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/IAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 237, 'http://purl.obolibrary.org/obo/MAXO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/MAXO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 238, 'http://purl.obolibrary.org/obo/MGPO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/MGPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 239, 'http://purl.obolibrary.org/obo/MOD_', 36, 5, '', '^http://purl\.obolibrary\.org/obo/MOD_[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 240, 'http://purl.obolibrary.org/obo/MONDO_', 38, 7, '', '^http://purl\.obolibrary\.org/obo/MONDO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 241, 'http://purl.obolibrary.org/obo/MP_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/MP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 242, 'http://purl.obolibrary.org/obo/MPATH_', 38, 0, '', '^http://purl\.obolibrary\.org/obo/MPATH_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 243, 'http://purl.obolibrary.org/obo/MS_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/MS_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 244, 'http://purl.obolibrary.org/obo/NBO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/NBO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 245, 'http://purl.obolibrary.org/obo/NCBITaxon_', 42, 0, '', '^http://purl\.obolibrary\.org/obo/NCBITaxon_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 246, 'http://purl.obolibrary.org/obo/NCIT_C', 38, 0, '', '^http://purl\.obolibrary\.org/obo/NCIT_C[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 247, 'http://purl.obolibrary.org/obo/NCIT_P', 38, 0, '', '^http://purl\.obolibrary\.org/obo/NCIT_P[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 248, 'http://purl.obolibrary.org/obo/NCIT_R', 38, 0, '', '^http://purl\.obolibrary\.org/obo/NCIT_R[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 249, 'http://purl.obolibrary.org/obo/OBA_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/OBA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 250, 'http://purl.obolibrary.org/obo/OBA_VT', 38, 7, '', '^http://purl\.obolibrary\.org/obo/OBA_VT[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 251, 'http://purl.obolibrary.org/obo/OBI_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/OBI_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 252, 'http://purl.obolibrary.org/obo/OGMS_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/OGMS_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 253, 'http://purl.obolibrary.org/obo/OMIM_', 37, 6, '', '^http://purl\.obolibrary\.org/obo/OMIM_[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 254, 'http://purl.obolibrary.org/obo/ONS_', 36, 0, '', '^http://purl\.obolibrary\.org/obo/ONS_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 255, 'http://purl.obolibrary.org/obo/PATO_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/PATO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 256, 'http://purl.obolibrary.org/obo/PHIPO_', 38, 7, '', '^http://purl\.obolibrary\.org/obo/PHIPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 257, 'http://purl.obolibrary.org/obo/PLANA_', 38, 7, '', '^http://purl\.obolibrary\.org/obo/PLANA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 258, 'http://purl.obolibrary.org/obo/PLANP_', 38, 7, '', '^http://purl\.obolibrary\.org/obo/PLANP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 259, 'http://purl.obolibrary.org/obo/PO_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/PO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 260, 'http://purl.obolibrary.org/obo/PR_', 35, 9, '', '^http://purl\.obolibrary\.org/obo/PR_[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 261, 'http://purl.obolibrary.org/obo/RO_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/RO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 262, 'http://purl.obolibrary.org/obo/RO_HOM', 38, 7, '', '^http://purl\.obolibrary\.org/obo/RO_HOM[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 263, 'http://purl.obolibrary.org/obo/SO_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/SO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 264, 'http://purl.obolibrary.org/obo/SYMP_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/SYMP_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 265, 'http://purl.obolibrary.org/obo/TRANS_', 38, 7, '', '^http://purl\.obolibrary\.org/obo/TRANS_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 266, 'http://purl.obolibrary.org/obo/UBERON_', 39, 7, '', '^http://purl\.obolibrary\.org/obo/UBERON_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 267, 'http://purl.obolibrary.org/obo/UO_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/UO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 268, 'http://purl.obolibrary.org/obo/UPHENO_', 39, 7, '', '^http://purl\.obolibrary\.org/obo/UPHENO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 269, 'http://purl.obolibrary.org/obo/WBbt_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/WBbt_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 270, 'http://purl.obolibrary.org/obo/WBls_', 37, 7, '', '^http://purl\.obolibrary\.org/obo/WBls_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 271, 'http://purl.obolibrary.org/obo/WBPhenotype_', 44, 7, '', '^http://purl\.obolibrary\.org/obo/WBPhenotype_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 272, 'http://purl.obolibrary.org/obo/XAO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/XAO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 273, 'http://purl.obolibrary.org/obo/XPO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/XPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 274, 'http://purl.obolibrary.org/obo/YPO_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/YPO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 275, 'http://purl.obolibrary.org/obo/ZFA_', 36, 7, '', '^http://purl\.obolibrary\.org/obo/ZFA_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 276, 'http://purl.obolibrary.org/obo/ZP_', 35, 7, '', '^http://purl\.obolibrary\.org/obo/ZP_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 400, 'http://identifiers.org/BTO:', 28, 7, '', '^http://identifiers\.org/BTO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 401, 'http://identifiers.org/chembl:CHEMBL', 37, 0, '', '^http://identifiers\.org/chembl:CHEMBL[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 402, 'http://identifiers.org/CL:', 27, 7, '', '^http://identifiers\.org/CL:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 403, 'http://identifiers.org/CLO:', 28, 7, '', '^http://identifiers\.org/CLO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 404, 'http://identifiers.org/ctd.gene:', 33, 0, '', '^http://identifiers\.org/ctd\.gene:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 405, 'http://identifiers.org/DOID:', 29, 0, '', '^http://identifiers\.org/DOID:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 406, 'http://identifiers.org/DOID:', 29, 7, '', '^http://identifiers\.org/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 407, 'http://identifiers.org/efo:', 28, 7, '', '^http://identifiers\.org/efo:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 408, 'http://identifiers.org/fb:FBgn', 31, 7, '', '^http://identifiers\.org/fb:FBgn[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 409, 'http://identifiers.org/hgnc:', 29, 0, '', '^http://identifiers\.org/hgnc:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 410, 'http://identifiers.org/HP:', 27, 7, '', '^http://identifiers\.org/HP:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 411, 'http://identifiers.org/hpa:ENSG00', 34, 9, '', '^http://identifiers\.org/hpa:ENSG00[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 412, 'http://identifiers.org/iuphar.receptor:', 40, 0, '', '^http://identifiers\.org/iuphar\.receptor:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 413, 'http://identifiers.org/kegg.disease:H', 38, 5, '', '^http://identifiers\.org/kegg\.disease:H[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 414, 'http://identifiers.org/lincs.cell:LCL-', 39, 0, '', '^http://identifiers\.org/lincs\.cell:LCL-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 415, 'http://identifiers.org/medgen:C', 32, 7, '', '^http://identifiers\.org/medgen:C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 416, 'http://identifiers.org/medgen:CN', 33, 6, '', '^http://identifiers\.org/medgen:CN[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 417, 'http://identifiers.org/MGI:', 28, 0, '', '^http://identifiers\.org/MGI:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 418, 'http://identifiers.org/mim:', 28, 0, '', '^http://identifiers\.org/mim:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 419, 'http://identifiers.org/NANDO:', 30, 0, '', '^http://identifiers\.org/NANDO:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 420, 'http://identifiers.org/ncbigene:', 33, 0, '', '^http://identifiers\.org/ncbigene:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 421, 'http://identifiers.org/ncit:C', 30, 0, '', '^http://identifiers\.org/ncit:C[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 422, 'http://identifiers.org/orphanet:', 33, 0, '', '^http://identifiers\.org/orphanet:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 423, 'http://identifiers.org/pharmgkb.disease:PA', 43, 0, '', '^http://identifiers\.org/pharmgkb\.disease:PA[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 424, 'http://identifiers.org/pharmgkb.gene:PA', 40, 0, '', '^http://identifiers\.org/pharmgkb\.gene:PA[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 425, 'http://identifiers.org/rgd:', 28, 0, '', '^http://identifiers\.org/rgd:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 426, 'http://identifiers.org/sgd:S', 29, 9, '', '^http://identifiers\.org/sgd:S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 427, 'http://identifiers.org/taxonomy/', 33, 0, '', '^http://identifiers\.org/taxonomy/([1-9][0-9]{0,8}|0)$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 428, 'http://identifiers.org/UBERON:', 31, 7, '', '^http://identifiers\.org/UBERON:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 429, 'http://identifiers.org/umls:C', 30, 7, '', '^http://identifiers\.org/umls:C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 430, 'http://identifiers.org/xenbase:XB-GENE-', 40, 0, '', '^http://identifiers\.org/xenbase:XB-GENE-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 431, 'http://identifiers.org/xenbase:XB-GENEPAGE-', 44, 0, '', '^http://identifiers\.org/xenbase:XB-GENEPAGE-[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 600, 'http://www.ebi.ac.uk/efo/CHEBI:', 32, 0, '', '^http://www\.ebi\.ac\.uk/efo/CHEBI:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 601, 'http://www.ebi.ac.uk/efo/DOID:', 31, 0, '', '^http://www\.ebi\.ac\.uk/efo/DOID:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 602, 'http://www.ebi.ac.uk/efo/EFO_', 30, 7, '', '^http://www\.ebi\.ac\.uk/efo/EFO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 603, 'http://www.ebi.ac.uk/efo/GO:', 29, 7, '', '^http://www\.ebi\.ac\.uk/efo/GO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 604, 'http://www.ebi.ac.uk/efo/MONDO:', 32, 7, '', '^http://www\.ebi\.ac\.uk/efo/MONDO:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 605, 'http://www.ebi.ac.uk/efo/swo/SWO_', 34, 7, '', '^http://www\.ebi\.ac\.uk/efo/swo/SWO_[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 606, 'http://www.ebi.ac.uk/efo/UBERON:', 33, 7, '', '^http://www\.ebi\.ac\.uk/efo/UBERON:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 607, 'http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MP&termId=MP:', 69, 7, '', '^http://www\.ebi\.ac\.uk/ontology-lookup/browse\.do\?ontName=MP&termId=MP:[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 608, 'http://www.ebi.ac.uk/swo/data/SWO_', 35, 0, '', '^http://www\.ebi\.ac\.uk/swo/data/SWO_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 609, 'https://www.ebi.ac.uk/chembl/cell_line_report_card/CHEMBL', 58, 0, '', '^https://www\.ebi\.ac\.uk/chembl/cell_line_report_card/CHEMBL[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 610, 'https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL', 55, 0, '', '^https://www\.ebi\.ac\.uk/chembl/target_report_card/CHEMBL[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 611, 'https://www.ebi.ac.uk/chembl/tissue_report_card/CHEMBL', 55, 0, '', '^https://www\.ebi\.ac\.uk/chembl/tissue_report_card/CHEMBL[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 800, 'http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#N0', 49, 9, '', '^http://evs\.nci\.nih\.gov/ftp1/NDF-RT/NDF-RT\.owl#N0[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 801, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C', 53, 0, '', '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#C[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 802, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P', 53, 0, '', '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#P[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 803, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R', 53, 0, '', '^http://ncicb\.nci\.nih\.gov/xml/owl/EVS/Thesaurus\.owl#R[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 804, 'http://www.ncbi.nlm.nih.gov/gene/', 34, 0, '', '^http://www\.ncbi\.nlm\.nih\.gov/gene/[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 805, 'https://rarediseases.info.nih.gov/diseases/', 44, 0, '/index', '^https://rarediseases\.info\.nih\.gov/diseases/[1-9][0-9]{0,8}/index$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 806, 'https://uts.nlm.nih.gov/uts/umls/concept/C', 43, 7, '', '^https://uts\.nlm\.nih\.gov/uts/umls/concept/C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 807, 'https://uts.nlm.nih.gov/uts/umls/concept/CN', 44, 6, '', '^https://uts\.nlm\.nih\.gov/uts/umls/concept/CN[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 808, 'https://www.ncbi.nlm.nih.gov/medgen/C', 38, 7, '', '^https://www\.ncbi\.nlm\.nih\.gov/medgen/C[0-9]{7}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values ( 809, 'https://www.ncbi.nlm.nih.gov/medgen/CN', 39, 6, '', '^https://www\.ncbi\.nlm\.nih\.gov/medgen/CN[0-9]{6}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1000, 'https://www.worldfloraonline.org/taxon/wfo-0', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-0[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1001, 'https://www.worldfloraonline.org/taxon/wfo-1', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-1[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1002, 'https://www.worldfloraonline.org/taxon/wfo-2', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-2[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1003, 'https://www.worldfloraonline.org/taxon/wfo-3', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-3[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1004, 'https://www.worldfloraonline.org/taxon/wfo-4', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-4[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1005, 'https://www.worldfloraonline.org/taxon/wfo-5', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-5[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1006, 'https://www.worldfloraonline.org/taxon/wfo-6', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-6[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1007, 'https://www.worldfloraonline.org/taxon/wfo-7', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-7[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1008, 'https://www.worldfloraonline.org/taxon/wfo-8', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-8[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1009, 'https://www.worldfloraonline.org/taxon/wfo-9', 45, 9, '', '^https://www\.worldfloraonline\.org/taxon/wfo-9[0-9]{9}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1200, 'http://www.wormbase.org/db/gene/gene?class=Gene;name=WBGene', 60, 8, '', '^http://www\.wormbase\.org/db/gene/gene\?class=Gene;name=WBGene[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1201, 'http://www.wormbase.org/db/gene/gene?name=WBGene', 49, 8, ';class=Gene', '^http://www\.wormbase\.org/db/gene/gene\?name=WBGene[0-9]{8};class=Gene$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1202, 'http://www.wormbase.org/species/c_elegans/gene/WBGene', 54, 8, '', '^http://www\.wormbase\.org/species/c_elegans/gene/WBGene[0-9]{8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1203, 'https://wormbase.org/db/seq/protein?class=Protein;name=BM', 58, 5, '', '^https://wormbase\.org/db/seq/protein\?class=Protein;name=BM[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1204, 'https://wormbase.org/db/seq/protein?class=Protein;name=CBP', 59, 5, '', '^https://wormbase\.org/db/seq/protein\?class=Protein;name=CBP[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1205, 'https://wormbase.org/db/seq/protein?class=Protein;name=CE', 58, 5, '', '^https://wormbase\.org/db/seq/protein\?class=Protein;name=CE[0-9]{5}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1400, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT1G', 61, 5, '', '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT1G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1401, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT2G', 61, 5, '', '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT2G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1402, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT3G', 61, 5, '', '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT3G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1403, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT4G', 61, 5, '', '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT4G[0-9]{5}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1404, 'https://bar.utoronto.ca/thalemine/portal.do?externalids=AT5G', 61, 5, '', '^https://bar\.utoronto\.ca/thalemine/portal\.do\?externalids=AT5G[0-9]{5}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1600, 'https://powo.science.kew.org/taxon/', 36, 0, '-1', '^https://powo\.science\.kew\.org/taxon/[1-9][0-9]{0,8}-1$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1601, 'https://powo.science.kew.org/taxon/', 36, 0, '-2', '^https://powo\.science\.kew\.org/taxon/[1-9][0-9]{0,8}-2$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1602, 'https://powo.science.kew.org/taxon/', 36, 0, '-3', '^https://powo\.science\.kew\.org/taxon/[1-9][0-9]{0,8}-3$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1603, 'https://powo.science.kew.org/taxon/', 36, 0, '-4', '^https://powo\.science\.kew\.org/taxon/[1-9][0-9]{0,8}-4$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1604, 'https://powo.science.kew.org/taxon/', 36, 0, '-5', '^https://powo\.science\.kew\.org/taxon/[1-9][0-9]{0,8}-5$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1800, 'http://glycosmos.org/glycogene/', 32, 0, '', '^http://glycosmos\.org/glycogene/[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1801, 'https://glycosmos.org/diseases/DOID:', 37, 0, '', '^https://glycosmos\.org/diseases/DOID:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1802, 'https://glycosmos.org/diseases/DOID:', 37, 7, '', '^https://glycosmos\.org/diseases/DOID:0[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1803, 'https://glycosmos.org/organisms/show/', 38, 0, '', '^https://glycosmos\.org/organisms/show/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1900, 'http://lincsportal.ccs.miami.edu/cells/#/view/ES-', 50, 0, '', '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/ES-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1901, 'http://lincsportal.ccs.miami.edu/cells/#/view/LCL-', 51, 0, '', '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LCL-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1902, 'http://lincsportal.ccs.miami.edu/cells/#/view/LPC-', 51, 0, '', '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LPC-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (1903, 'http://lincsportal.ccs.miami.edu/cells/#/view/LSC-', 51, 0, '', '^http://lincsportal\.ccs\.miami\.edu/cells/#/view/LSC-[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2000, 'http://www.orpha.net/consor/cgi-bin/OC_Exp.php?Expert=', 55, 0, '', '^http://www\.orpha\.net/consor/cgi-bin/OC_Exp\.php\?Expert=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2001, 'http://www.orpha.net/ORDO/Orphanet_', 36, 0, '', '^http://www\.orpha\.net/ORDO/Orphanet_[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2002, 'https://www.orpha.net/consor/cgi-bin/OC_Exp.php?Expert=', 56, 0, '', '^https://www\.orpha\.net/consor/cgi-bin/OC_Exp\.php\?Expert=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2003, 'https://www.orpha.net/en/disease/detail/', 41, 0, '', '^https://www\.orpha\.net/en/disease/detail/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2100, 'https://www.ipni.org/n/', 24, 0, '-1', '^https://www\.ipni\.org/n/[1-9][0-9]{0,8}-1$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2101, 'https://www.ipni.org/n/', 24, 0, '-2', '^https://www\.ipni\.org/n/[1-9][0-9]{0,8}-2$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2102, 'https://www.ipni.org/n/', 24, 0, '-3', '^https://www\.ipni\.org/n/[1-9][0-9]{0,8}-3$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2103, 'https://www.ipni.org/n/', 24, 0, '-4', '^https://www\.ipni\.org/n/[1-9][0-9]{0,8}-4$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2200, 'http://www.yeastgenome.org/cgi-bin/locus.fpl?dbid=S', 52, 9, '', '^http://www\.yeastgenome\.org/cgi-bin/locus\.fpl\?dbid=S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2201, 'http://www.yeastgenome.org/locus/S', 35, 9, '', '^http://www\.yeastgenome\.org/locus/S[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2202, 'https://www.yeastgenome.org/locus/S', 36, 9, '', '^https://www\.yeastgenome\.org/locus/S[0-9]{9}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2300, 'https://glyconnect.expasy.org/all/cell_lines/', 46, 0, '', '^https://glyconnect\.expasy\.org/all/cell_lines/[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2301, 'https://glyconnect.expasy.org/all/proteins/', 44, 0, '', '^https://glyconnect\.expasy\.org/all/proteins/[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2302, 'https://glyconnect.expasy.org/all/taxonomies/', 46, 0, '', '^https://glyconnect\.expasy\.org/all/taxonomies/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2400, 'http://purl.bioontology.org/ontology/NDFRT/N0', 46, 9, '', '^http://purl\.bioontology\.org/ontology/NDFRT/N0[0-9]{9}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2401, 'http://purl.bioontology.org/ontology/SNOMEDCT/', 47, 0, '', '^http://purl\.bioontology\.org/ontology/SNOMEDCT/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2500, 'http://rgd.mcw.edu/rgdweb/report/gene/main.html?id=', 52, 0, '', '^http://rgd\.mcw\.edu/rgdweb/report/gene/main\.html\?id=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2501, 'https://rgd.mcw.edu/rgdweb/report/gene/main.html?id=', 53, 0, '', '^https://rgd\.mcw\.edu/rgdweb/report/gene/main\.html\?id=[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2600, 'http://semanticscience.org/resource/CHEMINF_', 45, 6, '', '^http://semanticscience\.org/resource/CHEMINF_[0-9]{6}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2601, 'http://semanticscience.org/resource/SIO_', 41, 6, '', '^http://semanticscience\.org/resource/SIO_[0-9]{6}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2700, 'http://www.genenames.org/cgi-bin/gene_symbol_report?hgnc_id=', 61, 0, '', '^http://www\.genenames\.org/cgi-bin/gene_symbol_report\?hgnc_id=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2701, 'https://www.genenames.org/data/gene-symbol-report/#!/hgnc_id/', 62, 0, '', '^https://www\.genenames\.org/data/gene-symbol-report/#!/hgnc_id/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2800, 'http://www.informatics.jax.org/marker/MGI:', 43, 0, '', '^http://www\.informatics\.jax\.org/marker/MGI:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2801, 'https://hpo.jax.org/app/browse/term/HP:', 40, 7, '', '^https://hpo\.jax\.org/app/browse/term/HP:[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2900, 'https://cancer.sanger.ac.uk/cell_lines/sample/overview?id=', 59, 0, '', '^https://cancer\.sanger\.ac\.uk/cell_lines/sample/overview\?id=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (2901, 'https://cellmodelpassports.sanger.ac.uk/passports/SIDM', 55, 5, '', '^https://cellmodelpassports\.sanger\.ac\.uk/passports/SIDM[0-9]{5}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3000, 'https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId=', 68, 0, '', '^https://guidetopharmacology\.org/GRAC/ObjectDisplayForward\?objectId=[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3001, 'https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=', 74, 0, '', '^https://www\.guidetopharmacology\.org/GRAC/DiseaseDisplayForward\?diseaseId=[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3100, 'https://www.disease-ontology.org/?id=DOID:', 43, 0, '', '^https://www\.disease-ontology\.org/\?id=DOID:[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3101, 'https://www.disease-ontology.org/?id=DOID:', 43, 7, '', '^https://www\.disease-ontology\.org/\?id=DOID:0[0-9]{6}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3200, 'https://www.pharmgkb.org/disease/PA', 36, 0, '', '^https://www\.pharmgkb\.org/disease/PA[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3201, 'https://www.pharmgkb.org/gene/PA', 33, 0, '', '^https://www\.pharmgkb\.org/gene/PA[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3300, 'https://www.xenbase.org/gene/showgene.do?method=display&geneId=XB-GENE-', 72, 0, '', '^https://www\.xenbase\.org/gene/showgene\.do\?method=display&geneId=XB-GENE-[1-9][0-9]{0,8}$');
insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3301, 'https://www.xenbase.org/gene/showgene.do?method=display&geneId=XB-GENEPAGE-', 76, 0, '', '^https://www\.xenbase\.org/gene/showgene\.do\?method=display&geneId=XB-GENEPAGE-[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3400, 'http://birdgenenames.org/cgnc/GeneReport?id=', 45, 0, '', '^http://birdgenenames\.org/cgnc/GeneReport\?id=[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3500, 'http://dictybase.org/gene/DDB_G', 32, 7, '', '^http://dictybase\.org/gene/DDB_G[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3600, 'http://edamontology.org/data_', 30, 0, '', '^http://edamontology\.org/data_[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3700, 'http://flybase.org/reports/FBgn', 32, 7, '', '^http://flybase\.org/reports/FBgn[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3800, 'http://nanbyodata.jp/ontology/NANDO_', 37, 7, '', '^http://nanbyodata\.jp/ontology/NANDO_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (3900, 'http://purl.uniprot.org/taxonomy/', 34, 0, '', '^http://purl\.uniprot\.org/taxonomy/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4000, 'http://rdf.glycoinfo.org/source/', 33, 0, '', '^http://rdf\.glycoinfo\.org/source/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4100, 'http://www.bioassayontology.org/bao#BAO_', 41, 7, '', '^http://www\.bioassayontology\.org/bao#BAO_[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4200, 'http://www.ecogene.org/gene/EG', 31, 5, '', '^http://www\.ecogene\.org/gene/EG[0-9]{5}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4300, 'http://www.ensembl.org/id/b', 28, 0, '', '^http://www\.ensembl\.org/id/b[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4400, 'http://www.ensemblgenomes.org/id/BMEI', 38, 0, '', '^http://www\.ensemblgenomes\.org/id/BMEI[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4500, 'http://www.wikidata.org/prop/direct/P', 38, 0, '', '^http://www\.wikidata\.org/prop/direct/P[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4600, 'https://ctdbase.org/detail.go?type=gene&acc=', 45, 0, '', '^https://ctdbase\.org/detail\.go\?type=gene&acc=[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4700, 'https://depmap.org/portal/cell_line/ACH-', 41, 6, '', '^https://depmap\.org/portal/cell_line/ACH-[0-9]{6}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4800, 'https://eol.org/pages/', 23, 0, '', '^https://eol\.org/pages/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (4900, 'https://lincs.hms.harvard.edu/db/cells/', 40, 0, '', '^https://lincs\.hms\.harvard\.edu/db/cells/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5000, 'https://monarchinitiative.org/disease/MONDO:', 45, 7, '', '^https://monarchinitiative\.org/disease/MONDO:[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5100, 'https://omim.org/entry/', 24, 6, '', '^https://omim\.org/entry/[0-9]{6}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5200, 'https://platform.opentargets.org/target/ENSG00', 47, 9, '', '^https://platform\.opentargets\.org/target/ENSG00[0-9]{9}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5300, 'https://search.thegencc.org/genes/HGNC:', 40, 0, '', '^https://search\.thegencc\.org/genes/HGNC:[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5400, 'https://www.cancerrxgene.org/translation/CellLine/', 51, 0, '', '^https://www\.cancerrxgene\.org/translation/CellLine/[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5500, 'https://www.drugbank.ca/bio_entities/BE', 40, 7, '', '^https://www\.drugbank\.ca/bio_entities/BE[0-9]{7}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5600, 'https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=', 80, 0, '', '^https://www\.itis\.gov/servlet/SingleRpt/SingleRpt\?search_topic=TSN&search_value=[1-9][0-9]{0,8}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5700, 'https://www.kegg.jp/entry/H', 28, 5, '', '^https://www\.kegg\.jp/entry/H[0-9]{5}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5800, 'https://www.nextprot.org/term/TS-', 34, 4, '', '^https://www\.nextprot\.org/term/TS-[0-9]{4}$');

insert into ontology.resource_categories__reftable(unit_id, prefix, value_offset, value_length, suffix, pattern) values (5900, 'https://www.proteinatlas.org/ENSG00', 36, 9, '', '^https://www\.proteinatlas\.org/ENSG00[0-9]{9}$');
