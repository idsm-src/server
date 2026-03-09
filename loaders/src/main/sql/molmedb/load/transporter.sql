insert into molmedb.transporter_bases
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



insert into molmedb.protein_bases
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
