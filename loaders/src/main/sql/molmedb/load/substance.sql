insert into molmedb.substance_bases
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



insert into molmedb.substance_identifiers
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



insert into molmedb.substance_links
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



insert into molmedb.obsoleted_substances
(
    identifier,
    substance_id
)
select
    identifier,
    structure_id
from molmedb_tmp.structure_links;
