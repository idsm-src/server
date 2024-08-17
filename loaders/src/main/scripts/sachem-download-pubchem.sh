#!/bin/bash

set -ueo pipefail

source datasource.properties

version=$(date -d yesterday +%Y-%m-%d)

output="$base/sachem/pubchem/base-$version"
mkdir -p "$base/sachem/pubchem"
mkdir "$output"

wget --progress=bar:force -P "$output" -r -nH --cut-dirs=4 ftp://ftp.ncbi.nlm.nih.gov/pubchem/Compound/CURRENT-Full/SDF

test -L "$base/pubchem" && rm "$base/pubchem"
ln -s "pubchem-$version" "$base/pubchem"
