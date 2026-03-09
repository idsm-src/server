insert into molmedb.reference_bases
(
    id,
    doi,
    pmid,
    citation
)
select
    id,
    doi,
    case identifier_source when 'MED' then identifier end,
    citation
from molmedb_tmp.publications
where id not in (22, 1030, 1031, 1875, 1876, 4308);



insert into molmedb.reference_bases
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



insert into molmedb.reference_substances
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



insert into molmedb.reference_membranes
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



insert into molmedb.reference_methods
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



insert into molmedb.reference_proteins
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
