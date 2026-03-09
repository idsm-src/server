insert into molmedb.interaction_bases
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



insert into molmedb.fluorescent_interaction_bases
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



insert into molmedb.membrane_bases
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
    membranes.name,
    nullif(membranes.abbreviation, membranes.name),
    nullif(membranes.description, '')
from
    molmedb_tmp.membranes as membranes
    left join (molmedb_tmp.categories as categories join molmedb_tmp.model_has_categories as model_categories
        on categories.type = 1 and model_categories.category_id = categories.id) on model_categories.model_id = membranes.id;



insert into molmedb.membrane_parts
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



insert into molmedb.method_bases
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

