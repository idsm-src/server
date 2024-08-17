#!/bin/bash

set -ueo pipefail

source datasource.properties


cat << EOF | psql -v ON_ERROR_STOP=on --echo-errors --host="$host" --port="$port" --dbname="$dbname" --username="$user"
create schema if not exists chembl;
create schema chembl_tmp;
EOF


TABLES="\
-t activities \
-t assays \
-t assay_type \
-t binding_sites \
-t bio_component_sequences \
-t biotherapeutic_components \
-t biotherapeutics \
-t cell_dictionary \
-t component_class \
-t component_sequences \
-t component_synonyms \
-t compound_properties \
-t compound_records \
-t compound_structures \
-t confidence_score_lookup \
-t docs \
-t drug_indication \
-t drug_mechanism \
-t frac_classification \
-t hrac_classification \
-t irac_classification \
-t molecule_atc_classification \
-t molecule_dictionary \
-t molecule_frac_classification \
-t molecule_hierarchy \
-t molecule_hrac_classification \
-t molecule_irac_classification \
-t molecule_references \
-t molecule_synonyms \
-t protein_classification \
-t relationship_type \
-t source \
-t target_components \
-t target_dictionary \
-t target_relations";

(tar xz -f "$base"/chembl/chembl_*_postgresql.tar.gz --to-stdout --wildcards 'chembl_*_postgresql.dmp' || test $? = 141) |
pg_restore --no-owner --no-comments -f - $TABLES |
sed -e '1,/-- Data for Name:/s#public#chembl_tmp#g' -e 's#^COPY public\.#COPY chembl_tmp.#' |
psql -a -U idsm -d idsm -v ON_ERROR_STOP=1 --host="$host" --port="$port" --dbname="$dbname" --username="$user"
