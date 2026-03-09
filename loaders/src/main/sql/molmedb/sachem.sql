create table molmedb.substance_smiles
(
    substance_id        integer not null,
    smiles              varchar not null,
    primary key(substance_id)
);


insert into molmedb.substance_smiles
(
    substance_id,
    smiles
)
select
    id,
    canonical_smiles
from molmedb_tmp.structures as structures
where canonical_smiles is not null and not exists (select 1 from molmedb_tmp.structure_links as links where links.identifier = structures.identifier);


select sachem.add_index('molmedb', 'molmedb', 'substance_smiles', 'substance_id', 'smiles', 16, 16, 100000, 0);
