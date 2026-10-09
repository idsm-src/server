-- Synchronizes the schema molmedb with the dump loaded into the schema molmedb_tmp, inside the transaction of
-- load-molmedb.sh. Each table is first filled into a temporary table of the same row type and primary key, so that
-- the values get exactly the stored types and duplicate keys fail, and then merged into the table, which inserts,
-- updates or deletes only the differing rows.


--- substance_bases ---

create temporary table substance_bases (like molmedb.substance_bases, primary key(id)) on commit drop;

insert into pg_temp.substance_bases
(
    id,
    parent_id,
    charge,
    ph_start,
    ph_end,
    molecular_weight,
    logp,
    identifier,
    canonical_smiles,
    inchi,
    inchikey
)
select
    id,
    parent_id,
    charge,
    ph_start,
    ph_end,
    molecular_weight,
    logp,
    identifier,
    canonical_smiles,
    inchi,
    inchikey
from molmedb_tmp.structures as structures
where not exists (select 1 from molmedb_tmp.structure_links as links where links.identifier = structures.identifier);

merge into molmedb.substance_bases as t
using pg_temp.substance_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        parent_id = s.parent_id,
        charge = s.charge,
        ph_start = s.ph_start,
        ph_end = s.ph_end,
        molecular_weight = s.molecular_weight,
        logp = s.logp,
        identifier = s.identifier,
        canonical_smiles = s.canonical_smiles,
        inchi = s.inchi,
        inchikey = s.inchikey
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- substance_identifiers ---

create temporary table substance_identifiers (like molmedb.substance_identifiers, primary key(substance_id, type, value)) on commit drop;

insert into pg_temp.substance_identifiers
(
    substance_id,
    type,
    value
)
select distinct
    identifiers.structure_id,
    type,
    case identifiers.type
        when 6 then 'CHEBI:' || replace(identifiers.value, 'CHEBI:', '')
        when 8 then 'CHEMBL' || replace(upper(identifiers.value), 'CHEMBL', '')
        else trim(identifiers.value)
    end
from molmedb_tmp.identifiers as identifiers
where identifiers.state != 3 and identifiers.type in (1,4,5,6,7,8) and
    not (identifiers.type = 6 and identifiers.value like 'CHEMBL%') and
    not exists (select 1 from molmedb_tmp.structures as structures, molmedb_tmp.structure_links as links
        where identifiers.structure_id = structures.id and links.identifier = structures.identifier);

merge into molmedb.substance_identifiers as t
using pg_temp.substance_identifiers as s on t.substance_id = s.substance_id and t.type = s.type and t.value = s.value
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- substance_links ---

create temporary table substance_links (like molmedb.substance_links, primary key(substance_id, type, value)) on commit drop;

insert into pg_temp.substance_links
(
    substance_id,
    type,
    value
)
select distinct
    identifiers.structure_id,
    identifiers.type,
    case identifiers.type
        when 4 then value
        when 5 then replace(trim(identifiers.value), 'DB', '')
        when 6 then replace(identifiers.value, 'CHEBI:', '')
        when 8 then replace(upper(identifiers.value), 'CHEMBL', '')
    end::integer
from molmedb_tmp.identifiers as identifiers
where identifiers.state != 3 and identifiers.type in (4,5,6,8) and
    not (identifiers.type = 5 and identifiers.value like 'DBMET%') and
    not (identifiers.type = 6 and identifiers.value like 'CHEMBL%') and
    not exists (select 1 from molmedb_tmp.structures as structures, molmedb_tmp.structure_links as links
        where identifiers.structure_id = structures.id and links.identifier = structures.identifier);

merge into molmedb.substance_links as t
using pg_temp.substance_links as s on t.substance_id = s.substance_id and t.type = s.type and t.value = s.value
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- obsoleted_substances ---

create temporary table obsoleted_substances (like molmedb.obsoleted_substances, primary key(identifier)) on commit drop;

insert into pg_temp.obsoleted_substances
(
    identifier,
    substance_id
)
select
    identifier,
    structure_id
from molmedb_tmp.structure_links;

merge into molmedb.obsoleted_substances as t
using pg_temp.obsoleted_substances as s on t.identifier = s.identifier
when matched and t is distinct from s then
    update set
        substance_id = s.substance_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- interaction_bases ---

create temporary table interaction_bases (like molmedb.interaction_bases, primary key(id)) on commit drop;

insert into pg_temp.interaction_bases
(
    id,
    substance_id,
    membrane_id,
    method_id,
    publication_id,
    model_publication_id,
    logk,
    logk_accuracy,
    logperm,
    logperm_accuracy,
    x_min,
    x_min_accuracy,
    gpen,
    gpen_accuracy,
    gwat,
    gwat_accuracy,
    temperature,
    ph,
    charge,
    comment
)
select
    interactions.id,
    interactions.structure_id,
    datasets.membrane_id,
    datasets.method_id,
    interactions.publication_id,
    model_publications.publication_id,
    interactions.logk,
    interactions.logk_accuracy,
    interactions.logperm,
    interactions.logperm_accuracy,
    interactions.x_min,
    interactions.x_min_accuracy,
    interactions.gpen,
    interactions.gpen_accuracy,
    interactions.gwat,
    interactions.gwat_accuracy,
    interactions.temperature,
    interactions.ph,
    translate(interactions.charge, '+ ', ''),
    nullif(interactions.note, '')
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.datasets as datasets, molmedb_tmp.model_has_publications as model_publications
where interactions.dataset_id = datasets.id and interactions.dataset_id = model_publications.model_id and model_publications.model_type = 'App\Models\Dataset';

merge into molmedb.interaction_bases as t
using pg_temp.interaction_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        substance_id = s.substance_id,
        membrane_id = s.membrane_id,
        method_id = s.method_id,
        publication_id = s.publication_id,
        model_publication_id = s.model_publication_id,
        logk = s.logk,
        logk_accuracy = s.logk_accuracy,
        logperm = s.logperm,
        logperm_accuracy = s.logperm_accuracy,
        x_min = s.x_min,
        x_min_accuracy = s.x_min_accuracy,
        gpen = s.gpen,
        gpen_accuracy = s.gpen_accuracy,
        gwat = s.gwat,
        gwat_accuracy = s.gwat_accuracy,
        temperature = s.temperature,
        ph = s.ph,
        charge = s.charge,
        comment = s.comment
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- fluorescent_interaction_bases ---

create temporary table fluorescent_interaction_bases (like molmedb.fluorescent_interaction_bases, primary key(id)) on commit drop;

insert into pg_temp.fluorescent_interaction_bases
(
    id,
    substance_id,
    membrane_id,
    method_id,
    publication_id,
    model_publication_id,
    theta,
    theta_accuracy,
    abs_wl,
    abs_wl_accuracy,
    fluo_wl,
    fluo_wl_accuracy,
    qy,
    qy_accuracy,
    lt,
    lt_accuracy,
    temperature,
    ph,
    charge,
    comment
)
select
    interactions.id,
    interactions.structure_id,
    datasets.membrane_id,
    datasets.method_id,
    interactions.publication_id,
    model_publications.publication_id,
    interactions.theta,
    interactions.theta_accuracy,
    interactions.abs_wl,
    interactions.abs_wl_accuracy,
    interactions.fluo_wl,
    interactions.fluo_wl_accuracy,
    interactions.qy,
    interactions.qy_accuracy,
    interactions.lt,
    interactions.lt_accuracy,
    interactions.temperature,
    interactions.ph,
    translate(interactions.charge, '+ ', ''),
    nullif(interactions.note, '')
from molmedb_tmp.fluorescent_properties as interactions, molmedb_tmp.datasets as datasets, molmedb_tmp.model_has_publications as model_publications
where interactions.dataset_id = datasets.id and interactions.dataset_id = model_publications.model_id and model_publications.model_type = 'App\Models\Dataset';

merge into molmedb.fluorescent_interaction_bases as t
using pg_temp.fluorescent_interaction_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        substance_id = s.substance_id,
        membrane_id = s.membrane_id,
        method_id = s.method_id,
        publication_id = s.publication_id,
        model_publication_id = s.model_publication_id,
        theta = s.theta,
        theta_accuracy = s.theta_accuracy,
        abs_wl = s.abs_wl,
        abs_wl_accuracy = s.abs_wl_accuracy,
        fluo_wl = s.fluo_wl,
        fluo_wl_accuracy = s.fluo_wl_accuracy,
        qy = s.qy,
        qy_accuracy = s.qy_accuracy,
        lt = s.lt,
        lt_accuracy = s.lt_accuracy,
        temperature = s.temperature,
        ph = s.ph,
        charge = s.charge,
        comment = s.comment
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- membrane_bases ---

create temporary table membrane_bases (like molmedb.membrane_bases, primary key(id)) on commit drop;

insert into pg_temp.membrane_bases
(
    id,
    category,
    parent_category,
    name,
    abbreviation,
    description
)
select
    membranes.id,
    categories.id,
    categories.parent_id,
    trim(membranes.name),
    nullif(membranes.abbreviation, membranes.name),
    nullif(membranes.description, '')
from
    molmedb_tmp.membranes as membranes
    left join (molmedb_tmp.categories as categories join molmedb_tmp.model_has_categories as model_categories
        on categories.type = 1 and model_categories.category_id = categories.id) on model_categories.model_id = membranes.id;

merge into molmedb.membrane_bases as t
using pg_temp.membrane_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        category = s.category,
        parent_category = s.parent_category,
        name = s.name,
        abbreviation = s.abbreviation,
        description = s.description
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- membrane_parts ---

create temporary table membrane_parts (like molmedb.membrane_parts, primary key(membrane_id, chebi_id)) on commit drop;

insert into pg_temp.membrane_parts
(
    membrane_id,
    chebi_id
)
values
    ( 1, 45240),
    ( 2, 28866),
    ( 2, 16113),
    ( 3, 74669),
    (13, 73001),
    (14, 72999),
    (15, 73001),
    (20, 16113),
    (24, 90027),
    (26, 35255),
    (27, 74669),
    (27, 74986),
    (27, 16113),
    (28, 16113),
    (29, 17578),
    (31, 16113),
    (37, 45296),
    (38, 28817),
    (39, 16113),
    (39, 183652),
    (43, 34083),
    (43, 73001),
    (44, 34083),
    (48, 73001),
    (48, 16113),
    (49, 65211),
    (51, 60568),
    (52, 183653),
    (53, 183654),
    (54, 16188);

merge into molmedb.membrane_parts as t
using pg_temp.membrane_parts as s on t.membrane_id = s.membrane_id and t.chebi_id = s.chebi_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- method_bases ---

create temporary table method_bases (like molmedb.method_bases, primary key(id)) on commit drop;

insert into pg_temp.method_bases
(
    id,
    category,
    parent_category,
    name,
    abbreviation,
    description
)
select
    methods.id,
    categories.id,
    categories.parent_id,
    methods.name,
    nullif(methods.abbreviation, methods.name),
    nullif(methods.description, '')
from
    molmedb_tmp.methods as methods
    left join (molmedb_tmp.categories as categories join molmedb_tmp.model_has_categories as model_categories
        on categories.type = 2 and model_categories.category_id = categories.id) on model_categories.model_id = methods.id;

merge into molmedb.method_bases as t
using pg_temp.method_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        category = s.category,
        parent_category = s.parent_category,
        name = s.name,
        abbreviation = s.abbreviation,
        description = s.description
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- transporter_bases ---

create temporary table transporter_bases (like molmedb.transporter_bases, primary key(id)) on commit drop;

insert into pg_temp.transporter_bases
(
    id,
    substance_id,
    protein_id,
    membrane_id,
    method_id,
    publication_id,
    model_publication_id,
    category,
    km,
    km_accuracy,
    ec50,
    ec50_accuracy,
    ki,
    ki_accuracy,
    ic50,
    ic50_accuracy,
    temperature,
    ph,
    charge,
    comment
)
select
    interactions.id,
    interactions.structure_id,
    interactions.protein_id,
    datasets.membrane_id,
    datasets.method_id,
    interactions.publication_id,
    model_publications.publication_id,
    interactions.category_id,
    interactions.km,
    interactions.km_accuracy,
    interactions.ec50,
    interactions.ec50_accuracy,
    interactions.ki,
    interactions.ki_accuracy,
    interactions.ic50,
    interactions.ic50_accuracy,
    interactions.temperature,
    interactions.ph,
    translate(interactions.charge, '+ ', ''),
    nullif(interactions.note, '')
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.datasets as datasets, molmedb_tmp.model_has_publications as model_publications
where interactions.dataset_id = datasets.id and interactions.dataset_id = model_publications.model_id and model_publications.model_type = 'App\Models\Dataset';

merge into molmedb.transporter_bases as t
using pg_temp.transporter_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        substance_id = s.substance_id,
        protein_id = s.protein_id,
        membrane_id = s.membrane_id,
        method_id = s.method_id,
        publication_id = s.publication_id,
        model_publication_id = s.model_publication_id,
        category = s.category,
        km = s.km,
        km_accuracy = s.km_accuracy,
        ec50 = s.ec50,
        ec50_accuracy = s.ec50_accuracy,
        ki = s.ki,
        ki_accuracy = s.ki_accuracy,
        ic50 = s.ic50,
        ic50_accuracy = s.ic50_accuracy,
        temperature = s.temperature,
        ph = s.ph,
        charge = s.charge,
        comment = s.comment
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- protein_bases ---

create temporary table protein_bases (like molmedb.protein_bases, primary key(id)) on commit drop;

insert into pg_temp.protein_bases
(
    id,
    uniprot_id,
    name
)
select
    proteins.id,
    proteins.uniprot_id,
    identifiers.value
from
    molmedb_tmp.proteins as proteins
    left join molmedb_tmp.protein_identifiers as identifiers
        on proteins.id = identifiers.protein_id and identifiers.type = 1 and identifiers.state = 2;

merge into molmedb.protein_bases as t
using pg_temp.protein_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        uniprot_id = s.uniprot_id,
        name = s.name
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- reference_bases ---

create temporary table reference_bases (like molmedb.reference_bases, primary key(id)) on commit drop;

insert into pg_temp.reference_bases
(
    id,
    doi,
    pmid,
    citation
)
select
    id,
    trim(doi),
    case identifier_source when 'MED' then identifier end,
    citation
from molmedb_tmp.publications
where id not in (22, 1030, 1031, 1875, 1876, 4308);

insert into pg_temp.reference_bases
(
    id,
    label,
    homepage
)
values
    (  22, 'MolMeDB',   'https://molmedb.upol.cz/'),
    (1030, 'PubChem',   'https://pubchem.ncbi.nlm.nih.gov/'),
    (1031, 'ChEMBL',    'https://www.ebi.ac.uk/chembl/'),
    (1875, 'IUPHAR',    'https://www.guidetopharmacology.org/'),
    (1876, 'Metrabase', 'http://www-metrabase.ch.cam.ac.uk/'),
    (4308, 'BindingDB', 'https://bindingdb.org/');

merge into molmedb.reference_bases as t
using pg_temp.reference_bases as s on t.id = s.id
when matched and t is distinct from s then
    update set
        doi = s.doi,
        pmid = s.pmid,
        citation = s.citation,
        label = s.label,
        homepage = s.homepage
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- reference_substances ---

create temporary table reference_substances (like molmedb.reference_substances, primary key(reference_id, substance_id)) on commit drop;

insert into pg_temp.reference_substances
(
    reference_id,
    substance_id
)
select interactions.publication_id, interactions.structure_id
from molmedb_tmp.interactions_active as interactions
where publication_id is not null
union
select interactions.publication_id, interactions.structure_id
from molmedb_tmp.interactions_passive as interactions
where publication_id is not null
union
select interactions.publication_id, interactions.structure_id
from molmedb_tmp.fluorescent_properties as interactions
where publication_id is not null
union
select interactions.publication_id, structures.parent_id
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.structures as structures
where interactions.structure_id = structures.id and interactions.publication_id is not null and structures.parent_id is not null
union
select interactions.publication_id, structures.parent_id
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.structures as structures
where interactions.structure_id = structures.id and interactions.publication_id is not null and structures.parent_id is not null
union
select interactions.publication_id, structures.parent_id
from molmedb_tmp.fluorescent_properties  as interactions, molmedb_tmp.structures as structures
where interactions.structure_id = structures.id and interactions.publication_id is not null and structures.parent_id is not null
union
select model_publications.publication_id, interactions.structure_id
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.model_has_publications as model_publications
where model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset'
union
select model_publications.publication_id, interactions.structure_id
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.model_has_publications as model_publications
where model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset'
union
select model_publications.publication_id, interactions.structure_id
from molmedb_tmp.fluorescent_properties as interactions, molmedb_tmp.model_has_publications as model_publications
where model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset'
union
select model_publications.publication_id, structures.parent_id
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.structures as structures, molmedb_tmp.model_has_publications as model_publications
where interactions.structure_id = structures. id and model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset' and structures.parent_id is not null
union
select model_publications.publication_id, structures.parent_id
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.structures as structures, molmedb_tmp.model_has_publications as model_publications
where interactions.structure_id = structures. id and model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset' and structures.parent_id is not null
union
select model_publications.publication_id, structures.parent_id
from molmedb_tmp.fluorescent_properties as interactions, molmedb_tmp.structures as structures, molmedb_tmp.model_has_publications as model_publications
where interactions.structure_id = structures. id and model_publications.model_id = interactions.dataset_id and model_publications.model_type = 'App\Models\Dataset' and structures.parent_id is not null;

merge into molmedb.reference_substances as t
using pg_temp.reference_substances as s on t.reference_id = s.reference_id and t.substance_id = s.substance_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- reference_membranes ---

create temporary table reference_membranes (like molmedb.reference_membranes, primary key(reference_id, membrane_id)) on commit drop;

insert into pg_temp.reference_membranes
(
    reference_id,
    membrane_id
)
select interactions.publication_id, datasets.membrane_id
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.datasets as datasets
where interactions.dataset_id = datasets.id and interactions.publication_id is not null
union
select interactions.publication_id, datasets.membrane_id
from molmedb_tmp.fluorescent_properties as interactions, molmedb_tmp.datasets as datasets
where interactions.dataset_id = datasets.id
union
select model_publications.publication_id, datasets.membrane_id
from molmedb_tmp.datasets as datasets, molmedb_tmp.model_has_publications as model_publications
where model_publications.model_id = datasets.id and model_publications.model_type = 'App\Models\Dataset' and datasets.membrane_id is not null;

merge into molmedb.reference_membranes as t
using pg_temp.reference_membranes as s on t.reference_id = s.reference_id and t.membrane_id = s.membrane_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- reference_methods ---

create temporary table reference_methods (like molmedb.reference_methods, primary key(reference_id, method_id)) on commit drop;

insert into pg_temp.reference_methods
(
    reference_id,
    method_id
)
select interactions.publication_id, datasets.method_id
from molmedb_tmp.interactions_passive as interactions, molmedb_tmp.datasets as datasets
where interactions.dataset_id = datasets.id and interactions.publication_id is not null
union
select interactions.publication_id, datasets.method_id
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.datasets as datasets
where interactions.dataset_id = datasets.id  and datasets.method_id is not null
union
select interactions.publication_id, datasets.method_id
from molmedb_tmp.fluorescent_properties as interactions, molmedb_tmp.datasets as datasets
where interactions.dataset_id = datasets.id
union
select model_publications.publication_id, datasets.method_id
from molmedb_tmp.datasets as datasets, molmedb_tmp.model_has_publications as model_publications
where model_publications.model_id = datasets.id and model_publications.model_type = 'App\Models\Dataset' and datasets.method_id is not null;

merge into molmedb.reference_methods as t
using pg_temp.reference_methods as s on t.reference_id = s.reference_id and t.method_id = s.method_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- reference_proteins ---

create temporary table reference_proteins (like molmedb.reference_proteins, primary key(reference_id, protein_id)) on commit drop;

insert into pg_temp.reference_proteins
(
    reference_id,
    protein_id
)
select interactions.publication_id, interactions.protein_id
from molmedb_tmp.interactions_active as interactions
union
select model_publications.publication_id, interactions.protein_id
from molmedb_tmp.interactions_active as interactions, molmedb_tmp.model_has_publications as model_publications
where interactions.dataset_id = model_publications.model_id and model_publications.model_type = 'App\Models\Dataset';

merge into molmedb.reference_proteins as t
using pg_temp.reference_proteins as s on t.reference_id = s.reference_id and t.protein_id = s.protein_id
when not matched by target then
    insert values (s.*)
when not matched by source then
    delete;


--- sachem index ---

select sachem.cleanup('molmedb');
select sachem.sync_data('molmedb', false, true);
