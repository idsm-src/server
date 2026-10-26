#!/bin/bash

set -ueo pipefail

source datasource.properties

# the greatest release directory, not the latest symlink, which EBI does not always move to a new release
release=$(wget -q -O - "https://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBL-RDF/" | sed -n 's|.*href="\([0-9][0-9.]*\)/".*|\1|p' | sort -V | tail -n 1)

if [ -z "$release" ]; then
    echo "the latest release of ChEMBL cannot be determined" >&2
    exit 1
fi

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
