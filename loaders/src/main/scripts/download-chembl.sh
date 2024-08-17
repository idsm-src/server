#!/bin/bash

set -ueo pipefail

source datasource.properties

version=$(echo -e "open ftp.ebi.ac.uk\nuser anonymous\ncd /pub/databases/chembl/ChEMBL-RDF\nls -l\nbye\n" | ftp -inv | sed -n '/latest -> /s/.*latest -> //p')

if [ -e "$base/chembl-$version" ]; then
    suffix=1
    while [ -e "$base/chembl-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/chembl-$version"
mkdir "$output"

wget --progress=bar:force -P "$output" -r -A 'chembl_*_postgresql.tar.gz' -nH --cut-dirs=5 "ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/latest/"
wget --progress=bar:force -P "$output"/rdf -r -A ttl.gz -nH --cut-dirs=5 "ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBL-RDF/latest/"

test -L "$base/chembl" && rm "$base/chembl"
ln -s "chembl-$version" "$base/chembl"
