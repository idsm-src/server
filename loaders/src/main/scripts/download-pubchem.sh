#!/bin/bash

set -ueo pipefail

source datasource.properties

version=$(wget -q ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF/void.ttl -O - | sed -n '/dcterms:modified/s/.*dcterms:modified."\([^"]*\)"\^\^xsd:date.*/\1/p')

if [ -e "$base/pubchem-$version" ]; then
    suffix=1
    while [ -e "$base/pubchem-$version.$suffix" ]; do
        suffix=$((suffix + 1))
    done
    version="$version.$suffix"
fi

output="$base/pubchem-$version"
mkdir "$output"

wget --progress=bar:force -P "$output" -r -l 3 -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF
wget --progress=bar:force -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF/compound/general
wget --progress=bar:force -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/RDF/descriptor

# the bioassays and the molfiles have no version of their own, the date of their download stands for it
date=$(date '+%Y-%m-%d')
wget --progress=bar:force -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/XML
echo "$date" | gzip > "$output/Bioassay/version.txt.gz"

date=$(date '+%Y-%m-%d')
wget --progress=bar:force -P "$output" -r -nH --cut-dirs=1 ftp://ftp.ncbi.nlm.nih.gov/pubchem/Compound/CURRENT-Full/SDF
echo "$date" | gzip > "$output/Compound/version.txt.gz"

(cd "$output/Compound/CURRENT-Full/SDF" && md5sum -c --quiet -- *.md5)

test -L "$base/pubchem" && rm "$base/pubchem"
ln -s "pubchem-$version" "$base/pubchem"
