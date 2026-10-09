#!/bin/bash

set -ueo pipefail

source datasource.properties

release=$(echo -e "open ftp.ebi.ac.uk\nuser anonymous\npassive\ncd /pub/databases/chembl/ChEMBL-RDF\nls -l\nbye\n" | ftp -inv | sed -n '/latest -> /s/.*latest -> //p')
version="$release"

if [ -e "$base/chembl-$version" ]; then
    suffix=1
    while [ -e "$base/chembl-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/chembl-$version"
mkdir "$output"

wget --progress=bar:force -P "$output"/rdf -r -A ttl.gz -nH --cut-dirs=5 "ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBL-RDF/$release/"

sdf="chembl_$(echo "$release" | sed 's/\.0$//; s/\./_/g')"

wget --progress=bar:force -P "$output"/sdf "ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/releases/$sdf/$sdf.sdf.gz"
wget --progress=bar:force -P "$output"/sdf "ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/releases/$sdf/checksums.txt"

test "$(sha256sum "$output/sdf/$sdf.sdf.gz" | cut -d' ' -f1)" = "$(awk -v f="$sdf.sdf.gz" '$2 == f {print $1}' "$output/sdf/checksums.txt")"

test -L "$base/chembl" && rm "$base/chembl"
ln -s "chembl-$version" "$base/chembl"
