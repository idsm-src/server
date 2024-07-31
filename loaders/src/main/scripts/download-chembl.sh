#!/bin/bash

err()
{
    echo "$@"
    exit 1
}

set -ue -o pipefail

if [[ $BASH_SOURCE = */* ]]; then
  base="${BASH_SOURCE%/*}/.."
else
  err "cannot detect source path"
fi

if [[ $# -gt 1 ]]; then
  err "two many arguments"
fi

output=${1:-data/chembl}

if [[ -e "$output" ]]; then
    err "output directory '$output' already exist"
fi

mkdir -p "$output"


wget -P "$output" -r -A 'chembl_*_postgresql.tar.gz' -nH --cut-dirs=5 ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/latest/
wget -P "$output"/rdf -r -A ttl.gz -nH --cut-dirs=5 ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBL-RDF/latest/
