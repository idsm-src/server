#!/bin/bash

set -ueo pipefail

source datasource.properties

version=$(date '+%Y-%m-%d')

if [ -e "$base/pdb-$version" ]; then
    suffix=1
    while [ -e "$base/pdb-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/pdb-$version"
mkdir "$output"

wget --progress=bar:force -P "$output" https://ftp.ebi.ac.uk/pub/databases/msd/pdbechem_v2/ccd/ccd.tar.gz

test -L "$base/pdb" && rm "$base/pdb"
ln -s "pdb-$version" "$base/pdb"
