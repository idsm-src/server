#!/bin/bash

set -ueo pipefail

source datasource.properties

psql -v ON_ERROR_STOP=on --echo-errors --host="$host" --port="$port" --dbname="$dbname" --username="$user" << EOF
begin;

delete from isdb.spectrum_bases;
delete from isdb.compound_bases;


create temporary table isdb_neg
(
    name               varchar not null,
    exactmass          real not null,
    molecular_formula  varchar not null,
    smiles             varchar not null,
    inchi              varchar not null,
    pepmass            real not null,
    spectrum           pgms.spectrum
);

\lo_import '$base/isdb/isdb_neg.mgf'

insert into isdb_neg select * from  pgms.mgf_populate_recordset(null::isdb_neg, :LASTOID);

insert into isdb.compound_bases(accession, exact_mass, formula, smiles, inchi)
  select name, exactmass, molecular_formula, smiles, inchi from isdb_neg;

insert into isdb.spectrum_bases(id, ionmode, pepmass, spectrum)
  select b.id, 'N', d.pepmass, d.spectrum from isdb_neg d, isdb.compound_bases b where b.accession = d.name;


create temporary table isdb_pos
(
    name               varchar not null,
    exactmass          real not null,
    molecular_formula  varchar not null,
    smiles             varchar not null,
    inchi              varchar not null,
    pepmass            real not null,
    spectrum           pgms.spectrum
);

\lo_import '$base/isdb/isdb_pos.mgf'

insert into isdb_pos select * from  pgms.mgf_populate_recordset(null::isdb_pos, :LASTOID);

insert into isdb.compound_bases(accession, exact_mass, formula, smiles, inchi)
  select name, exactmass, molecular_formula, smiles, inchi from isdb_pos on conflict do nothing;

insert into isdb.spectrum_bases(id, ionmode, pepmass, spectrum)
  select b.id, 'P', d.pepmass, d.spectrum from isdb_pos d, isdb.compound_bases b where b.accession = d.name;


refresh materialized view isdb.compound_pubchem_compounds;
refresh materialized view isdb.compound_wikidata_compounds;
select sachem.sync_data('isdb');

commit;
EOF
